package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    FlightRepository flightRepository;

    @Autowired
    CloudinaryConfig cloudc;

    // It’s listFlights() method will handle all incoming requests to URI "/".
    // It accepts a Model and returns a view name list. This view name is resolved with configured view resolvers.

    @RequestMapping("/")
    public String listFlights(Model model){
        model.addAttribute("flights", flightRepository.findAll());
        return "list";
    }
    @PostMapping("/searchlist")
    public String search(Model model, @RequestParam("search") String search){
        model.addAttribute("flights" , flightRepository.findBydepartingAirportContainingIgnoreCaseOrAirlineContainingIgnoreCase(search,search));
        return "searchlist";
    }
    @GetMapping("/add")
    public String flightForm(Model model){
        model.addAttribute("flight", new Flight());
        return "flightForm";
    }
    @PostMapping("/process")
    public String processFlight(@Valid @ModelAttribute Flight flight, BindingResult result,
                                 @RequestParam("file") MultipartFile file){
        if(result.hasErrors()){
            return "flightForm";
        }
        if(file.isEmpty()){
            flightRepository.save(flight);
            return "redirect:/";
        }
        try{
            Map uploadResult = cloudc.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            flight.setFlightshot(uploadResult.get("url").toString());
            flightRepository.save(flight);
        }catch(IOException e){
            e.printStackTrace();
            return "flightForm";
        }
        return "redirect:/";
    }

    @RequestMapping("/detail/{id}")
    public String showFlight(@PathVariable("id") long id, Model model){
        model.addAttribute("flight", flightRepository.findById(id).get());
        return "show";
    }
    @RequestMapping("/update/{id}")
    public String updateFlight(@PathVariable("id") long id, Model model){
        model.addAttribute("flight" , flightRepository.findById(id).get());
        return "flightForm";
    }
    @RequestMapping("/delete/{id}")
    public String deleteFlight(@PathVariable("id") long id, Model model){
        flightRepository.deleteById(id);
        return "redirect:/";
    }
}
