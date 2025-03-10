package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.*;
import com.elice.aurasphere.contents.entity.File;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.entity.View;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.FileRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.contents.repository.ViewRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.global.s3.service.S3Service;
import com.elice.aurasphere.user.entity.Profile;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.ProfileRepository;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PostService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final FileRepository fileRepository;
    private final ViewRepository viewRepository;
    private final LikeService likeService;
    private final ViewService viewService;
    private final S3Service s3Service;



    private final PostMapper mapper;


    public PostService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PostRepository postRepository,
            FileRepository fileRepository,
            ViewRepository viewRepository,
            LikeService likeService,
            ViewService viewService,
            S3Service s3Service,
            PostMapper mapper) {

        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.fileRepository = fileRepository;
        this.viewRepository = viewRepository;
        this.likeService = likeService;
        this.viewService = viewService;
        this.s3Service = s3Service;
        this.mapper = mapper;

    }

    public void incrementViewCnt(
            String username, Long postId,
            HttpServletRequest request,
            HttpServletResponse response
    ){

        Cookie oldCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postView")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + postId + "]")) {
                viewCountUp(postId);
                oldCookie.setValue(oldCookie.getValue() + "_[" + postId + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(oldCookie);
            }
        } else {
            viewCountUp(postId);
            Cookie newCookie = new Cookie("postView","[" + postId + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }
    }

    public void viewCountUp(Long postId){

        //Post를 찾을 수 없는 경우
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        View view = viewRepository.findByPostId(postId);
        view.countUp();
        viewRepository.save(view);
    }


    public PostListResponseDTO getFilteredPosts(
            String username, int size, Long postCursor, Optional<Long> filterCursor, Optional<String> filter
    ){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        FilterResponseDTO results;

        if(filter.isPresent() && filter.get().equals("likes")){
            results = postRepository.findPostsByLikes(user.getId(), size, postCursor, filterCursor);
        }else if(filter.isPresent() && filter.get().equals("views")){
            results = postRepository.findAllPostsByViews(user.getId(), size, postCursor, filterCursor);
        }else if(filter.isPresent() && filter.get().equals("following")){
            results = postRepository.findAllPostsByFollowing(user.getId(), size, postCursor, filterCursor);
        }else {
            results = postRepository.findAllPostsByAsc(user.getId(), size, postCursor);
        }

        //리스트가 비어있는 경우 hasNext를 false로 반환하고 리턴
        if(results.getPostList().isEmpty())
            return PostListResponseDTO.builder().hasNext(false).build();

        List<PostResponseDTO> posts = results.getPostList().stream().map(post -> {

            List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

            Profile profile = profileRepository.findByUserId(post.getUser().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

            return PostResponseDTO.builder()
                    .id(post.getId())
                    .nickname(profile.getNickname())
                    .profileUrl(profile.getProfileUrl())
                    .content(post.getContent())
                    .likeCnt(likeService.getLikeCnt(post.getId()))
                    .viewCnt(viewService.getViewCnt(post.getId()))
                    .isLiked(!likeService.isNotAlreadyLike(user,post))
                    .urls(urls)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = results.getPostList().get(results.getPostList().size() - 1).getId();
        boolean hasNext = results.getPostList().size() >= size;


        return PostListResponseDTO.builder()
                .postList(posts)
                .post_cursor(lastCursor)
                .filter_cursor(results.getFilterCursor())
                .hasNext(hasNext)
                .build();

    }


    /*
    내가 쓴 게시글 조회
    */
    public PostListResponseDTO getMyPosts(String username, int size, Long cursor){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> postList = postRepository.findMyPosts(user.getId(), size, cursor);

        //리스트가 비어있는 경우 hasNext를 false로 반환하고 리턴
        if(postList.isEmpty())
            return PostListResponseDTO.builder().hasNext(false).build();

        List<PostResponseDTO> posts = postList.stream().map(post -> {

            List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

            Profile profile = profileRepository.findByUserId(post.getUser().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

            return PostResponseDTO.builder()
                    .id(post.getId())
                    .nickname(profile.getNickname())
                    .profileUrl(profile.getProfileUrl())
                    .content(post.getContent())
                    .likeCnt(likeService.getLikeCnt(post.getId()))
                    .viewCnt(viewService.getViewCnt(post.getId()))
                    .isLiked(!likeService.isNotAlreadyLike(user,post))
                    .urls(urls)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = postList.get(postList.size() - 1).getId();
        boolean hasNext = postList.size() >= size;


        return PostListResponseDTO.builder()
                .postList(posts)
                .post_cursor(lastCursor)
                .hasNext(hasNext)
                .build();
    }

    /*
    *
    * 테스트코드 사용 X
    *
    */
    public PostListResponseDTO getTestPosts(String username, Pageable pageable){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<Post> postList = postRepository.findByDeletedDateIsNullOrderByCreatedAtDesc(pageable);

        //리스트가 비어있는 경우 hasNext를 false로 반환하고 리턴
        if(postList.isEmpty())
            return PostListResponseDTO.builder().hasNext(false).build();

        List<PostResponseDTO> posts = postList.stream().map(post -> {

            List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

            Profile profile = profileRepository.findByUserId(post.getUser().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

            return PostResponseDTO.builder()
                    .id(post.getId())
                    .nickname(profile.getNickname())
                    .profileUrl(profile.getProfileUrl())
                    .content(post.getContent())
                    .likeCnt(likeService.getLikeCnt(post.getId()))
                    .viewCnt(viewService.getViewCnt(post.getId()))
                    .isLiked(!likeService.isNotAlreadyLike(user,post))
                    .urls(urls)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }).toList();


        return PostListResponseDTO.builder()
                .postList(posts)
                .build();
    }

    //상세 게시글 조회
    public PostResponseDTO getPost(String username, Long postId) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if(post.getDeletedDate() != null){
            throw new CustomException(ErrorCode.POST_ALREADY_DELETED);
        }

        Profile profile = profileRepository.findByUserId(post.getUser().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

        return PostResponseDTO.builder()
                .id(post.getId())
                .nickname(profile.getNickname())
                .profileUrl(profile.getProfileUrl())
                .content(post.getContent())
                .likeCnt(likeService.getLikeCnt(postId))
                .viewCnt(viewService.getViewCnt(postId))
                .isLiked(!likeService.isNotAlreadyLike(user,post))
                .urls(urls)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }


    @Transactional
    //게시글 생성
    public PostResponseDTO registerPost(
            String username,
            String content,
            List<MultipartFile> files
    ) throws IOException {

        if(files.size() > 6){
            throw new CustomException(ErrorCode.TOO_MANY_FILES);
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));


        Post registeredPost = postRepository.save(
                Post.builder()
                        .user(user)
                        .content(content)
                        .build()
        );

        viewRepository.save(View.builder()
                .post(registeredPost)
                .viewCnt(0L)
                .build());

        List<FileDTO> urls = new ArrayList<>();

        List<File> fileList = new ArrayList<>();
        for (MultipartFile file : files) {
            log.info("contentType : {}", file.getContentType());

            if (file.getContentType().startsWith("image/")) {
                // 이미지 처리
                String imgUrl = s3Service.uploadFile(file, "image"); // S3에 업로드
                File imageFile = File.builder()
                        .post(registeredPost)
                        .url(imgUrl)
                        .fileType(File.FileType.IMAGE)
                        .build();

                fileList.add(imageFile);
                urls.add(FileDTO.builder()
                        .fileType(File.FileType.IMAGE)
                        .url(imgUrl)
                        .build());

            } else if (file.getContentType().startsWith("video/")) {
                // 비디오 처리
                String videoUrl = s3Service.uploadFile(file, "video"); // S3에 업로드
                File videoFile = File.builder()
                        .post(registeredPost)
                        .url(videoUrl)
                        .fileType(File.FileType.VIDEO)
                        .build();

                fileList.add(videoFile);
                urls.add(FileDTO.builder()
                        .fileType(File.FileType.VIDEO)
                        .url(videoUrl)
                        .build());
            }

        }
        fileRepository.saveAll(fileList); // 이미지 리스트 저장

        return PostResponseDTO.builder()
                .id(registeredPost.getId())
                .nickname(profile.getNickname())
                .profileUrl(profile.getProfileUrl())
                .content(registeredPost.getContent())
                .likeCnt(likeService.getLikeCnt(registeredPost.getId()))
                .viewCnt(viewService.getViewCnt(registeredPost.getId()))
                .commentCnt(0L)
                .urls(urls)
                .createdAt(registeredPost.getCreatedAt())
                .updatedAt(registeredPost.getUpdatedAt())
                .build();
    }

    //게시글 수정
    public PostResponseDTO editPost(String username, Long postId, PostUpdateDTO postUpdateDTO) {

        //유저를 찾을 수 없는 경우
        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        //프로필을 찾을 수 없는 경우
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        //Post를 찾을 수 없는 경우
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        //User와 Post 작성자가 일치하지 않는 경우
        if(!post.getUser().getEmail().equals(user.getEmail()))
            throw new CustomException(ErrorCode.USER_NOT_MATCH);

        return postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.updatePost(postUpdateDTO.getContent());

                    Post savedPost = postRepository.save(existingPost);

                    List<FileDTO> urls = fileRepository.findFilesByPostId(savedPost.getId());

                    return PostResponseDTO.builder()
                            .id(savedPost.getId())
                            .nickname(profile.getNickname())
                            .profileUrl(profile.getProfileUrl())
                            .content(savedPost.getContent())
                            .likeCnt(likeService.getLikeCnt(savedPost.getId()))
                            .viewCnt(viewService.getViewCnt(savedPost.getId()))
                            .urls(urls)
                            .isLiked(!likeService.isNotAlreadyLike(user, savedPost))
                            .createdAt(savedPost.getCreatedAt())
                            .updatedAt(savedPost.getUpdatedAt())
                            .build();
                })
                .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
    }

    public Long removePost(String username, Long postId){

        //유저를 찾을 수 없는 경우
        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        //Post를 찾을 수 없는 경우
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        //User와 Post 작성자가 일치하지 않는 경우
        if(!post.getUser().getEmail().equals(user.getEmail()))
            throw new CustomException(ErrorCode.USER_NOT_MATCH);

        post.removePost();

        Post deletedPost = postRepository.save(post);

        return deletedPost.getId();
    }
}