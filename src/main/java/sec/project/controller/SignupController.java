package sec.project.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.catalina.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Comment;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;
import sec.project.repository.CommentRepository;

@Controller
public class SignupController {
    
    @Autowired
    private SignupRepository signupRepository;
    @Autowired
    private CommentRepository commentRepository;
    private Signup newSignUp;
    
    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    /*
     * Returns the users profile 
     */
    @RequestMapping(value = "/profile/{username}", method = RequestMethod.GET)
    public String getUserProfile(@PathVariable String username, Model model) {
        if (checkLoginStatus()) {
            
            List<Comment> userCommentList = new ArrayList<>();
            for (Comment c : commentRepository.findAll()) {
                if (c.getUsername().equals(username)) {
                    userCommentList.add(c);
                }
            }
            model.addAttribute("comments", userCommentList);
            System.out.println("Fetched user's comments!");
            return "profile";
        } else {
            return "form";
        }
    }

    /*
    * Returns the current user's profile
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String getUserProfile() {
        if (checkLoginStatus()) {
            return "redirect:/profile/" + newSignUp.getName();
        } else {
            return "form";
        }
    }

    /*
    * Returns the main page with commens
     */
    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String main(Model model) {
        if (checkLoginStatus()) {
            model.addAttribute("comments", commentRepository.findAll());
            return "main";
        } else {
            return "form";
        }
    }

    /*
    * logs out the user
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout() {
        this.newSignUp = null;
        return "form";
    }

    /*
    * deletes the user
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String delete() {
        if (checkLoginStatus()) {
            for (Comment c : commentRepository.findAll()) {
                if (c.getUsername().equals(this.newSignUp.getName())) {
                    commentRepository.delete(c);
                }
            }
            for (Signup s : signupRepository.findAll()) {
                if (s.getName().equals(this.newSignUp.getName())) {
                    signupRepository.delete(s);
                    this.newSignUp = null;
                    return "deleted";
                }
            }
        }
        
        return "main";
    }


    /*
     * Returns the form to log in
     */
    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        System.out.println("loading form");
        return "form";
    }
    
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String submitForm(@RequestParam String name, @RequestParam String password) {
        for (Signup s : signupRepository.findAll()) {
            if (s.getName().equals(name)) {   
                return "username_exists";
            }
        }
        this.newSignUp = new Signup(name, password);
        signupRepository.save(this.newSignUp);
        System.out.println("Signup passed, going to main");
        return "signup_passed";
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam String name, @RequestParam String password) {
        
        for (Signup s : signupRepository.findAll()) {
            if (s.getName().equals(name) && s.getPassword().equals(password)) {
                System.out.println("login passed, going to main");
                return "redirect:/main";
            }
        }
        System.out.println("User not found!");
        return "login_error";
    }
    
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public String submitComment(@RequestParam String comment) {
        commentRepository.save(new Comment(comment, newSignUp.getName()));
        System.out.println("COmment passed, going to main");
        return "redirect:/main";
    }
    
    @RequestMapping(value = "/comments/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable Long id) {
        if (id != null) {
            for (Comment c : commentRepository.findAll()) {
                if (c.getId() == id) {
                    commentRepository.delete(id);
                }
            }
        }
        return "redirect:/profile/" + newSignUp.getName();
    }
    
    public boolean checkLoginStatus() {
        return this.newSignUp != null;
    }

    /*
    * STUFF TO MAKE SESSION ID SHORT
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setTomcatContextCustomizers(Arrays.asList(new ContextCustomizer()));
        return factory;
    }
    
    static class ContextCustomizer implements TomcatContextCustomizer {
        
        @Override
        public void customize(Context context) {
            // allow Javascript to access cookies
            context.setUseHttpOnly(false);
            System.out.println("SessionIdLength " + context.getManager().getSessionIdGenerator().getSessionIdLength());
            // set the sessionLength
            context.getManager().getSessionIdGenerator().setSessionIdLength(1);
            System.out.println("SessionIdLength " + context.getManager().getSessionIdGenerator().getSessionIdLength());
        }
    }
}
