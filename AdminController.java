package com.xworkz.issuemanagement.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.xworkz.issuemanagement.dto.*;
import com.xworkz.issuemanagement.model.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@Slf4j
@RequestMapping("/")
@SessionAttributes("adminDTO")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private HttpSession httpSession;




    public AdminController()
    {
        log.info("AdminController constructor:");
    }
    @PostMapping("admin")
    public String Admin(@RequestParam String email, @RequestParam  String password, RedirectAttributes redirectAttributes, Model model)
    {
        String adminUsername=adminService.getAdminName(email, password);
        log.info("username:{}",adminUsername);

        //to get admin name
        httpSession.setAttribute("AdminName",adminUsername);

         boolean data=adminService.findByEmailAndPassword(email,password);
        if (data) {
            log.info("findByEmailAndPassword successful in AdminController..");

            //return "AdminProfilePage";
            return "redirect:/AdminProfilePage";
        } else {
            log.info("findByEmailAndPassword not successful in AdminController");
            redirectAttributes.addFlashAttribute("errorAdminMessage", "Failed to login. Please check your email and password.");

            return "redirect:/adminPage";
        }


    }
    @GetMapping("adminPage")
public String admin()
{
    return "AdminPage";
}

    @GetMapping("AdminProfilePage")
    public String AdminProfilePage(Model model)
    {
        // Retrieve adminUsername from the session
        String adminUsername = (String) httpSession.getAttribute("AdminName");

        if (adminUsername != null)
        {
            // Adding attributes to the model to display on the profile page
            model.addAttribute("AdminProfilePageMessage", "Welcome to Admin profile");
            model.addAttribute("username", adminUsername);

        }
        else
        {
            // If adminDTO is not found in session, redirect to login page or handle accordingly
            return "redirect:/adminPage";
        }

        return "AdminProfilePage";
    }


    //view user details(SignUp details)
@GetMapping("viewUserDetails")
public String viewUserDetails(SignUpDTO signUpDTO,Model model)
{
    log.info("viewUserDetails method in AdminController..");
    List<SignUpDTO> signUpDTOData = adminService.findByUserId(signUpDTO);

    if (signUpDTOData != null) {
        log.info("viewUserDetails successful in AdminController..");
        model.addAttribute("ViewUserDetails", signUpDTOData);
        return "AdminViewUserDetails";
    } else {
        log.info("view-user-details not  successful in AdminController..");
    }
    return"AdminViewUserDetails";
}

@GetMapping("viewSubAdminDepartmentDetails")
public String viewSubAdminDepartmentDetails(Model model)
{
    log.info("viewSubAdminDepartmentDetails method in AdminController..");
    List<RegDeptAdminDTO> regDeptAdminDTOS = adminService.getAllSubAdminDetails();

    if (regDeptAdminDTOS != null) {
        log.info("viewSubAdminDepartmentDetails successful in AdminController..");
        model.addAttribute("subDepartmentDetails", regDeptAdminDTOS);
        return "AdminViewSubAdminDepartmentDetails";
    } else {
        log.info("viewSubAdminDepartmentDetails not  successful in AdminController..");
    }

    return "AdminViewSubAdminDepartmentDetails";
}


    //view Raise complaint details
    //IllegalStateException: Ambiguous mapping while same action in two methods
    //displaying viewComplaintRaiseDetails and searchByComplaintTypeAndCity in same jsp page so, we make sure model key same in both method
    @GetMapping("viewComplaintRaiseDetails")
    public String viewComplaintRaiseDetails(ComplaintRaiseDTO complaintRaiseDTO,RedirectAttributes redirectAttributes, Model model, DepartmentDTO departmentDTO)
    {
        List<ComplaintRaiseDTO> listOfCities=adminService.findAllCities();

        log.info("viewUserDetails method running in AdminController");

        List<ComplaintRaiseDTO> viewData = adminService.findByComplaintRaiseId(complaintRaiseDTO);

        // Fetch the list of complaints and departments
        List<DepartmentDTO> departments = adminService.findByDepartmentName();

        if (viewData!=null || departments!=null) {
            model.addAttribute("viewRaiseComplaint", viewData);
            model.addAttribute("departments", departments);// Fetch the list of departments for departmentNames
            // departments
            model.addAttribute("cities",listOfCities);

            log.info("View raise complaint data successful in AdminController");
            //return "redirect:ViewComplaintRaise";// data will gone so, use model(viewName problem)
            return "AdminViewComplaintRaiseDetails";
        } else {
            log.info("View raise complaint data not successful in AdminController.");
        }

        //return "redirect:ViewComplaintRaise";
        return"AdminViewComplaintRaiseDetails";
    }

//    @GetMapping("ViewComplaintRaise")
//    public String viewComplaintRaiseDetails()
//    {
//        return "AdminViewComplaintRaiseDetails";
//    }

    @PostMapping("searchComplaintTypeAndCity")
    public String searchByComplaintTypeAndCity(ComplaintRaiseDTO complaintRaiseDTO, DepartmentDTO departmentDTO, Model model) {

        log.info("searchByComplaintType method running in AdminController..!!");

        // Always fetch the list of departments and cities
        List<DepartmentDTO> departments = adminService.findByDepartmentName();
        List<ComplaintRaiseDTO> listOfCities=adminService.findAllCities();
        model.addAttribute("cities",listOfCities);
        model.addAttribute("departments", departments); // Add department list to the model


        // Search by complaint type and city
        List<ComplaintRaiseDTO> listOfTypeAndCity = adminService.searchByComplaintTypeAndCity(complaintRaiseDTO.getComplaintType(), complaintRaiseDTO.getCity());

        if (!listOfTypeAndCity.isEmpty())
        {
            model.addAttribute("viewRaiseComplaint", listOfTypeAndCity);
            return "AdminViewComplaintRaiseDetails";
        }
        else
        {
            // If no results found, search by complaint type OR city
            List<ComplaintRaiseDTO> listOfTypeOrCity = adminService.searchByComplaintTypeOrCity(complaintRaiseDTO.getComplaintType(), complaintRaiseDTO.getCity());
            if (!listOfTypeOrCity.isEmpty())
            {
                model.addAttribute("viewRaiseComplaint", listOfTypeOrCity);
                return "AdminViewComplaintRaiseDetails";
            }
            else
            {
                // If no records found, add a message to the model
                model.addAttribute("NoComplaints", "No Records Found");
            }
        }


        return "AdminViewComplaintRaiseDetails";
    }

    @GetMapping("adminViewComplaintRaiseDetails")
    public String adminViewComplaintRaiseDetails()
    {
        return "AdminViewComplaintRaiseDetails";
    }


    @PostMapping("saveDepartment")
    public String saveDepartment(DepartmentDTO departmentDTO, RedirectAttributes redirectAttributes, RegDeptAdminDTO regDeptAdminDTO)
{
   DepartmentDTO data= adminService.saveDepartment(departmentDTO);
    log.info("saveDepartment method running in AdminController..");


    if (data != null) {
        log.info("saveDepartment successful in AdminController..");
        redirectAttributes.addFlashAttribute("msgDepartment", "Successfully added department ");
//          return  "AdminAddComplaints";
        return "redirect:/saveDepartment";

    } else {
        log.info("saveDepartment not successful in AdminController..");

        redirectAttributes.addFlashAttribute("errorDepartment", "not Successfully added department");
    }

    //return "AdminAddComplaints";
    return "redirect:/saveDepartment";

}
@GetMapping("saveDepartment")
    public String saveDepartment()
{
    return "AdminAddDepartment";
}




@PostMapping("updateDepartment")
    public String updateComplaint(@RequestParam("complaintId") int complaintId,@RequestParam("departmentId") int departmentId,ComplaintRaiseDTO complaintRaiseDTO,RedirectAttributes redirectAttributes)
{

   boolean data= adminService.updateStatusAndDepartmentId(complaintId,departmentId,complaintRaiseDTO.getStatus());
   if(data)
   {
       log.info("update:"+data);
   }
   else {
       log.info("No update:" + data);
   }
    redirectAttributes.addFlashAttribute("msg","Updated Successfully");
    return "redirect:/viewComplaintRaiseDetails";
}

    @GetMapping("UpdateDepartment")
    public String updateDepartment()
    {
        return "AdminViewComplaintRaiseDetails";
    }





}

