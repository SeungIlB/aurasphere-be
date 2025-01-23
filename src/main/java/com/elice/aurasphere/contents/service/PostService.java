package com.elice.aurasphere.contents.service;


import com.elice.aurasphere.contents.dto.*;
import com.elice.aurasphere.contents.entity.File;
import com.elice.aurasphere.contents.entity.Post;
import com.elice.aurasphere.contents.mapper.PostMapper;
import com.elice.aurasphere.contents.repository.FileRepository;
import com.elice.aurasphere.contents.repository.PostRepository;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.global.s3.service.S3Service;
import com.elice.aurasphere.user.entity.User;
import com.elice.aurasphere.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FileRepository fileRepository;
    private final LikeService likeService;
    private final S3Service s3Service;


    private final PostMapper mapper;


    public PostService(
            UserRepository userRepository,
            PostRepository postRepository,
            FileRepository fileRepository,
            LikeService likeService,
            S3Service s3Service,
            PostMapper mapper) {

        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.fileRepository = fileRepository;
        this.likeService = likeService;
        this.s3Service = s3Service;
        this.mapper = mapper;

    }

    public void incrementViewCnt(String username, Long postId, HttpServletRequest request, HttpServletResponse response){

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

        postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.viewCntUp();

                    Post updatedPost = postRepository.save(existingPost);

                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));

    }


    public PostListResDTO getFilteredPosts(String username, int size, Long cursor, String filter){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> postList;

        postList = postRepository.findAllPostsByAsc(user.getId(), size, cursor);

//        switch (filter) {
//            case "likes":
//                postList = postRepository.findPostsByLikes();
//                break;
//            case "views":
//                postList = postRepository.findPostsByViews();
//                break;
//            case "following":
//                Long userId = userDetails.getId();
//                postList = postRepository.findPostsByFollowing(userId);
//                break;
//            default:
//                postList = postRepository.findAllPostsByAsc();
//                break;
//        }

        //리스트가 비어있는 경우 hasNext를 false로 반환하고 리턴
        if(postList.isEmpty())
            return PostListResDTO.builder().hasNext(false).build();

        List<PostResDTO> posts = postList.stream().map(post -> {

            List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

            return PostResDTO.builder()
                    .id(post.getId())
                    .content(post.getContent())
                    .likeCnt(post.getLikeCnt())
                    .isLiked(!likeService.isNotAlreadyLike(user,post))
                    .urls(urls)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = postList.get(postList.size() - 1).getId();
        boolean hasNext = postList.size() >= size;


        return PostListResDTO.builder()
                .postList(posts)
                .cursor(lastCursor)
                .hasNext(hasNext)
                .build();

    }



    public PostListResDTO getMyPosts(String username, int size, Long cursor){

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Post> postList = postRepository.findMyPosts(user.getId(), size, cursor);

        //리스트가 비어있는 경우 hasNext를 false로 반환하고 리턴
        if(postList.isEmpty())
            return PostListResDTO.builder().hasNext(false).build();

        List<PostResDTO> posts = postList.stream().map(post -> {

            List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

            return PostResDTO.builder()
                    .id(post.getId())
                    .content(post.getContent())
                    .likeCnt(post.getLikeCnt())
                    .isLiked(!likeService.isNotAlreadyLike(user,post))
                    .urls(urls)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }).toList();

        Long lastCursor = postList.get(postList.size() - 1).getId();
        boolean hasNext = postList.size() >= size;


        return PostListResDTO.builder()
                .postList(posts)
                .cursor(lastCursor)
                .hasNext(hasNext)
                .build();
    }

    //상세 게시글 조회
    public PostResDTO getPost(String username, Long postId) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<FileDTO> urls = fileRepository.findFilesByPostId(post.getId());

        return PostResDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .likeCnt(post.getLikeCnt())
                .isLiked(!likeService.isNotAlreadyLike(user,post))
                .urls(urls)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }


    @Transactional
    //게시글 생성
    public PostResDTO registerPost(
            String username,
            String content,
            List<MultipartFile> files
    ) throws IOException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post registeredPost = postRepository.save(
                Post.builder()
                        .user(user)
                        .content(content)
                        .likeCnt(0L)
                        .viewCnt(0L)
                        .build()
        );

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

        return PostResDTO.builder()
                .id(registeredPost.getId())
                .content(registeredPost.getContent())
                .likeCnt(registeredPost.getLikeCnt())
                .viewCnt(registeredPost.getViewCnt())
                .commentCnt(0L)
                .urls(urls)
                .createdAt(registeredPost.getCreatedAt())
                .updatedAt(registeredPost.getUpdatedAt())
                .build();
    }

    //게시글 수정
    public PostResDTO editPost(String username, Long postId, PostUpdateDTO postUpdateDTO) {

        //유저를 찾을 수 없는 경우
        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        //Post를 찾을 수 없는 경우
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        //User와 Post 작성자가 일치하지 않는 경우
        if(!post.getUser().getEmail().equals(user.getEmail()))
            throw new CustomException(ErrorCode.USER_NOT_MATCH);

        return postRepository.findById(postId)
                .map(existingPost -> {

                    existingPost.updatePost(postUpdateDTO.getContent());

                    Post updatedPost = postRepository.save(existingPost);

                    return mapper.postToPostResDto(updatedPost);
                })
                .orElseThrow(() -> new CustomException(ErrorCode.POST_UPDATE_FAILED));
    }
}