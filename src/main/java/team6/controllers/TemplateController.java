package team6.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import team6.factories.TemplateFactoryWrapper;
import team6.models.TemplateInterface;
import team6.repositories.NARsTemplateRepository;
import team6.throwables.IllegalTemplateException;
import team6.util.SheetAdapterWrapper;

@Controller
public class TemplateController {

    @Autowired
    private NARsTemplateRepository nARsTemplateRepository;

    public CrudRepository getRepositoryClass(String template) {
        switch (template) {
            case "NARs":
                return nARsTemplateRepository;
        }
        throw new IllegalArgumentException();
    }


    @GetMapping("/templates")
    public String readAllView() {
        return "templates/type-list";
    }

    @PostMapping("/templates")
    public String uploadFile(Model model, MultipartFile file, @RequestParam String templateType) throws IOException, IllegalTemplateException {
        //Converting the multipart file into a filestream, to be parseable
        InputStream in = file.getInputStream();
        BufferedReader fileRead = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        // send BufferedReader to SheetAdapterWrapper
        SheetAdapterWrapper saw = new SheetAdapterWrapper();
        // get HashMap representation from the wrapper
        List<HashMap<String,String>> dataMap = saw.parse("csv", fileRead, 1, 3);
        // send list of maps to factory
        TemplateFactoryWrapper templateFactoryWrapper = new TemplateFactoryWrapper();
        CrudRepository templateRepository = getRepositoryClass(templateType);
        for(HashMap<String,String> item : dataMap){
            // System.out.println(item.toString());
            TemplateInterface templateInterface = templateFactoryWrapper.build(templateType, item);
            templateRepository.save(templateInterface);
        }
        model.addAttribute("file", result);
        return "templates/type-list";
    }

    @GetMapping("/templates/NARs")
    public String readAllNARsView(Model model) {
	return "templates/type-list";
    }
}