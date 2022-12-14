package com.OurInternfactory.Controllers;

import com.OurInternfactory.Exceptions.ResourceNotFoundException;
import com.OurInternfactory.Models.User;
import com.OurInternfactory.Payloads.*;
import com.OurInternfactory.Repositories.UserRepo;
import com.OurInternfactory.Security.JwtTokenHelper;
import com.OurInternfactory.Services.FileServices;
import com.OurInternfactory.Services.UserService;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final FileServices fileServices;
    @Value("${project.image}")
    private String path;
    private final UserRepo userRepo;
    public final JwtTokenHelper jwtTokenHelper;
    public UserController(UserService userService, FileServices fileServices, UserRepo userRepo, JwtTokenHelper jwtTokenHelper) {
        this.userService = userService;
        this.fileServices = fileServices;
        this.userRepo = userRepo;
        this.jwtTokenHelper = jwtTokenHelper;
    }
//Delete - delete user
    @DeleteMapping("/deleteUser")
    public ResponseEntity<ApiResponse> deleteUser(@RequestHeader("Authorization") String bearerToken){
        String userEmail= this.jwtTokenHelper.getUsernameFromToken(bearerToken.substring(7));
        User user = this.userRepo.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User", "Email: "+userEmail, 0));
        this.userService.DeleteUser(user.getId());
        return new ResponseEntity<>(new ApiResponse("User Deleted Successfully", true), HttpStatus.OK);
    }
//Delete - delete user
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteUserAdmin/{UserId}")
    public ResponseEntity<?> deleteAUser(@PathVariable("UserId") Integer UserId){
        this.userService.DeleteUser(UserId);
        return new ResponseEntity<>(new ApiResponse("User Deleted Successfully", true), HttpStatus.OK);
    }
//GET - user get all the users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }
//Get a user using user Id
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getSingleUser(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(this.userService.getUserById(userId));
    }
//Edit a user account by the user
    @PreAuthorize("hasAnyRole('NORMAL', 'ADMIN')")
    @PutMapping("/editUserInfo")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody EditUserDto editUserDto, @RequestHeader("Authorization") String bearerToken){
        editUserDto.setEmail(this.jwtTokenHelper.getUsernameFromToken(bearerToken.substring(7)));
        String message = this.userService.updateUserProfile(editUserDto);
        ApiResponse apiResponse = new ApiResponse(message, true);
        return ResponseEntity.ok(apiResponse);
    }
//get the user profile using the user email or username
    @GetMapping("/getUserInfo")
    public ResponseEntity<GetProfileResponse> getUserInfo(@RequestHeader("Authorization") String bearerToken){
        String finalEmail= this.jwtTokenHelper.getUsernameFromToken(bearerToken.substring(7));
        User user = this.userRepo.findByEmail(finalEmail).orElseThrow(() -> new ResourceNotFoundException("User", "Email :" + finalEmail, 0));
        GetProfileResponse editUserDto = new GetProfileResponse(user.getId(), user.getProfilePhoto(), user.getFirstname(), user.getLastname(), user.getGender(), user.getEmail(), user.getPhoneNumber());
        return new ResponseEntity<>(editUserDto, HttpStatus.OK);
    }
//To upload the profile photo
    @PutMapping("/setprofilephoto")
    public ResponseEntity<FileDto> settProfileImage(
            @RequestParam("image") MultipartFile image, @RequestHeader("Authorization") String bearerToken){
        String userEmail= this.jwtTokenHelper.getUsernameFromToken(bearerToken.substring(7));
        String filename = null;
        if (image.getContentType().equals("image/png") || image.getContentType().equals("image/jpg") || image.getContentType().equals("image/jpeg")) {
            User user = this.userRepo.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User", "Email :" + userEmail, 0));
            try {
                filename = this.fileServices.uploadImage(path, image);
                user.setProfilePhoto(filename);
                this.userRepo.save(user);
            }
            catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(new FileDto(filename, "Image not uploaded, Server error !!!"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(new FileDto(filename, "Image is Successfully Uploaded !!!"), HttpStatus.OK);
        }
        else{
                return new ResponseEntity<>(new FileDto(filename, "File is not of image type(JPEG/ JPG or PNG)!!!"), HttpStatus.BAD_REQUEST);
        }
    }
// To enable two-step verification
    @PutMapping("/twoStepEnable")
    public ResponseEntity<?> EnableTwoStep(@RequestHeader("Authorization") String bearerToken){
        String userEmail= this.jwtTokenHelper.getUsernameFromToken(bearerToken.substring(7));
        User user = this.userRepo.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User", "Email: "+userEmail, 0));
        if(user.getPhoneNumber() != null) {
            if (user.getTwoStepVerification()) {
                user.setTwoStepVerification(false);
                this.userRepo.save(user);
                return new ResponseEntity<>(new ApiResponse("Two step verification has been disabled", true), HttpStatus.OK);
            } else {
                user.setTwoStepVerification(true);
                this.userRepo.save(user);
                return new ResponseEntity<>(new ApiResponse("Two step verification has been enabled", true), HttpStatus.OK);
            }
        }
        else{
            return new ResponseEntity<>(new ApiResponse("Please complete the user profile first!", true), HttpStatus.BAD_REQUEST);
        }
    }
}
//    To view the profile photo
//    @GetMapping(value = "/getprofilephoto", produces = MediaType.IMAGE_JPEG_VALUE)
//    public void getProfileImage(@RequestBody ForgetEmail forgetEmail, HttpServletResponse response) throws IOException {
//        User user = this.userRepo.findByEmail(forgetEmail.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User", "Email :"+forgetEmail.getEmail(), 0));
//        InputStream resource = this.fileServices.getImage(path, user.getProfilePhoto());
//        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
//        StreamUtils.copy(resource, response.getOutputStream());
//    }

