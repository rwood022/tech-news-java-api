package com.technews.controller;

import com.technews.model.Comment;
import com.technews.model.Post;
import com.technews.model.User;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.Thymeleaf;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//@Controller indicates that these controllers will control flow for the front-end user experience.
@Controller
public class HomePageController {
//@Autowired informs Spring to scan for objects that need to be instantiated for a class or method to run
    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    CommentRepository commentRepository;

//login endpoint, which will allow users to log in by calling the login.html template when the /login route is hit.
// Model model, the first argument passed into the login() method, is inherited functionality from Spring—we've remapped Model to the model variable. Next, the addAttribute() method, built into the Model object, sends information to the Thymeleaf templates. In this case, model.addAttribute() will send a newly created user to the template as the string user, so that it can be displayed within the template
//Once logged in, the user will be redirected to the / route, bringing us to the homepage.
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {

        if (request.getSession(false) != null) {
            return "redirect:/";
        }

        model.addAttribute("user", new User());
        return "login";
    }

// When the logout route is hit, it checks whether the session exists. If it does, it invalidates the session, subsequently logging out the user. then redirects the user back to the login route.
    @GetMapping("/users/logout")
    public String logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        return "redirect:/login";
    }


//This endpoint will show users the homepage, which will require it to retrieve all the Post data. So inside the if conditional, we check that the user is logged in. Once that's confirmed, we create a variable called postList with a type List<Post>. Then we use a for loop to get all of the posts and populate them into the postList variable. We use model.addAttributes() to add these details to the User model. As a result, this route will populate and display the homepage.html template.
    @GetMapping("/")
    public String homepageSetup(Model model, HttpServletRequest request) {
        User sessionUser = new User();

        if (request.getSession(false) != null) {
            sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        } else {
            model.addAttribute("loggedIn", false);
        }


        List<Post> postList = postRepository.findAll();
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getById(p.getUserId());
            p.setUserName(user.getUsername());
        }

        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());

        // "point" and "points" attributes refer to upvotes.
        model.addAttribute("point", "point");
        model.addAttribute("points", "points");

        return "homepage";
    }
//All of the methods associated with the dashboard page will be called by the setupDashboardPage() method within this route. As the name suggests, this method helps set up the user dashboard.
    @GetMapping("/dashboard")
    public String dashboardPageSetup(Model model, HttpServletRequest request) throws Exception {

        if (request.getSession(false) != null) {
            setupDashboardPage(model, request);
            return "dashboard";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }

// creates an error message for the user to fill out all the criteria
    @GetMapping("/dashboardEmptyTitleAndLink")
    public String dashboardEmptyTitleAndLinkHandler(Model model, HttpServletRequest request) throws Exception {
        setupDashboardPage(model, request);
        model.addAttribute("notice", "To create a post the Title and Link must be populated!");
        return "dashboard";
    }


    @GetMapping("/singlePostEmptyComment/{id}")
    public String singlePostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
        return "single-post";
    }


    @GetMapping("/post/{id}")
    public String singlePostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        setupSinglePostPage(id, model, request);
        return "single-post";
    }


    @GetMapping("/editPostEmptyComment/{id}")
    public String editPostEmptyCommentHandler(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            model.addAttribute("notice", "To add a comment you must enter the comment in the comment text area!");
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }


    @GetMapping("/dashboard/edit/{id}")
    public String editPostPageSetup(@PathVariable int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            setupEditPostPage(id, model, request);
            return "edit-post";
        } else {
            model.addAttribute("user", new User());
            return "login";
        }
    }


//assign the value of the current user, via SESSION_USER, to a variable called sessionUser
    public Model setupDashboardPage(Model model, HttpServletRequest request) throws Exception {
        User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");

        Integer userId = sessionUser.getId();

//postRepository.findAllPostsByUserId(userId);, when we use a for loop to gather all posts that have been made by the specific userId.
        List<Post> postList = postRepository.findAllPostsByUserId(userId);
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            User user = userRepository.getById(p.getUserId());
            p.setUserName(user.getUsername());
        }
//model.addAttribute() method allows us to pass information into the Thymeleaf pages when that template is called
        model.addAttribute("user", sessionUser);
        model.addAttribute("postList", postList);
        model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        model.addAttribute("post", new Post());

        return model;
    }


    public Model setupSinglePostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
        }

        Post post = postRepository.getById(id);
        post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));

        User postUser = userRepository.getById(post.getUserId());
        post.setUserName(postUser.getUsername());

        List<Comment> commentList = commentRepository.findAllCommentsByPostId(post.getId());

        model.addAttribute("post", post);

        model.addAttribute("commentList", commentList);
        model.addAttribute("comment", new Comment());

        return model;
    }


    public Model setupEditPostPage(int id, Model model, HttpServletRequest request) {
        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");

            Post returnPost = postRepository.getById(id);
            User tempUser = userRepository.getById(returnPost.getUserId());
            returnPost.setUserName(tempUser.getUsername());
            returnPost.setVoteCount(voteRepository.countVotesByPostId(returnPost.getId()));

            List<Comment> commentList = commentRepository.findAllCommentsByPostId(returnPost.getId());

            model.addAttribute("post", returnPost);
            model.addAttribute("loggedIn", sessionUser.isLoggedIn());
            model.addAttribute("commentList", commentList);
            model.addAttribute("comment", new Comment());
        }

        return model;
    }
}
