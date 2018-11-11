package team6.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import team6.factories.TemplateFactoryWrapper;
import team6.models.ClientProfileTemplate;
import team6.models.NARsTemplate;
import team6.models.Organization;
import team6.models.Template;
import team6.repositories.ClientProfileTemplateRepository;
import team6.repositories.NARsTemplateRepository;
import team6.repositories.OrganizationRepository;
import team6.throwables.IllegalTemplateException;
import team6.util.AttributeResolver;
import team6.util.JoinTable;
import team6.util.SheetAdapterWrapper;
import team6.util.parameters.GroupParameter;
import team6.util.parameters.JoinParameter;
import team6.util.parameters.SelectParameter;
import team6.util.parameters.SortParameter;
import team6.util.parameters.WhereParameter;

@Controller
public class TemplateController {
    @Autowired
    private NARsTemplateRepository narsTemplateRepository;
    @Autowired
    private ClientProfileTemplateRepository clientProfileTemplateRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @GetMapping("/templates")
    public String readAllView() {
        return "templates/type-list";
    }

    @PostMapping("/templates")
    public String createTemplateByFile(Model model, MultipartFile file, @RequestParam String organizationId,
            @RequestParam String templateType) throws IOException, IllegalTemplateException {
        // Converting the multipart file into a filestream, to be parseable
        InputStream in = file.getInputStream();
        String fileName = file.getOriginalFilename();

        BufferedReader fileRead = new BufferedReader(new InputStreamReader(in));
        // send BufferedReader to SheetAdapterWrapper
        SheetAdapterWrapper saw = new SheetAdapterWrapper();

        // get HashMap representation from the wrapper
        List<HashMap<String, String>> dataMap = saw.parse(file.getContentType(), fileRead, 1, 3);

        TemplateFactoryWrapper templateFactoryWrapper = new TemplateFactoryWrapper();

        // find which repository is needed to be saved to:
        CrudRepository templateRepository = getRepo(templateType);

        // get the organization for this template:
        Long orgId = Long.parseLong(organizationId);
        Optional<Organization> org = organizationRepository.findById(orgId);
        Organization organization = org.get();

        ArrayList<Template> templateList = new ArrayList<Template>();
        // first make sure that all templates are converted as the right template
        for (HashMap<String, String> item : dataMap) {
            // find out which template it is and store it in respective repo
            try{
                Template template = templateFactoryWrapper.build(templateType, item, organization);
                templateList.add(template);
            }catch(IllegalTemplateException | IllegalArgumentException e){
                model.addAttribute("templateType" ,templateType.toString());
                model.addAttribute("fileName" ,fileName.toString());
                return "error-template-upload-fail";
            }
            
        }
        for (Template template : templateList){
            templateRepository.save(template);
        }
        // model.addAttribute("file", result);
        return "templates/type-list";
    }

    @GetMapping("/templates/NARs")
    public String readAllNARsView(Model model, @RequestParam Optional<String> select,
            @RequestParam Optional<String> where, @RequestParam Optional<String> sort,
            @RequestParam Optional<String> sortDirection, @RequestParam Optional<String> group,
            @RequestParam Optional<String> join) throws IllegalTemplateException {
        model.addAttribute("templateName", "Needs Assessment & Referrals");
        return templateReadList(model, select, where, sort, sortDirection, group, join, new NARsTemplate(), narsTemplateRepository);
    }

    @GetMapping("/templates/clientProfile")
    public String readAllClientProfileView(Model model, @RequestParam Optional<String> select,
            @RequestParam Optional<String> where, @RequestParam Optional<String> sort,
            @RequestParam Optional<String> sortDirection, @RequestParam Optional<String> group,
            @RequestParam Optional<String> join) throws IllegalTemplateException {
        model.addAttribute("templateName", "Client Profiles");
        return templateReadList(model, select, where, sort, sortDirection, group, join, new ClientProfileTemplate(), clientProfileTemplateRepository);
    }

    private String templateReadList(Model model, @RequestParam Optional<String> select,
            @RequestParam Optional<String> where, @RequestParam Optional<String> sort,
            @RequestParam Optional<String> sortDirection, @RequestParam Optional<String> group,
            @RequestParam Optional<String> join,
            Template template, JpaRepository repository) throws IllegalTemplateException {
        Sort sortObj = SortParameter.parse(sort.orElse("id"), sortDirection.orElse("asc"));
        final List<Object> rows = repository.findAll(sortObj);
        Map<String, String> attributeToFriendlyNameMap = new HashMap<>();
        List<String> attributeNames = template.getAttributeNames();
        List<String> friendlyNames = template.getFriendlyNames();
        if (join.isPresent()) {
            List<JoinTable> joinTables = JoinParameter.parse(join.get());
            for (JoinTable joinTable : joinTables) {
                String entity = joinTable.getEntity();
                JpaRepository otherRepository = getRepo(entity);
                Template otherTemplate = new TemplateFactoryWrapper().build(entity, new HashMap<>(), null);
                final List<Object> otherRows = otherRepository.findAll();
                attributeToFriendlyNameMap.putAll(otherTemplate.getAttributeToFriendlyNameMap());
                attributeNames.addAll(otherTemplate.getAttributeNames());
                friendlyNames.addAll(otherTemplate.getFriendlyNames());
            }
        }
        attributeToFriendlyNameMap.putAll(template.getAttributeToFriendlyNameMap());
        if (select.isPresent()) {
            attributeNames = SelectParameter.parse(select.get());
            friendlyNames = attributeNames.stream().map(attributeToFriendlyNameMap::get).collect(Collectors.toList());
        }
        Iterable<Object> filteredRows = rows;
        if (where.isPresent()) {
            filteredRows = () -> StreamSupport.stream(rows.spliterator(), false)
                .filter(row -> WhereParameter.parse(where.get()).populateWithObject(row).isTrue()).iterator();
        }
        List<List<Object>> rowGroups = new ArrayList<>();
        if (group.isPresent()) {
            String groupBy = GroupParameter.parse(group.get());
            final HashMap<Object, List<Object>> rowGroupsMap = new HashMap<>();
            filteredRows.forEach(row -> {
                Object attribute = AttributeResolver.get(groupBy, row);
                List<Object> rowGroup = (List<Object>) rowGroupsMap.getOrDefault(attribute, new ArrayList<>());
                rowGroup.add(row);
                rowGroupsMap.put(attribute, rowGroup);
            });
            rowGroups = new ArrayList<>(rowGroupsMap.values());
        } else {
            List<Object> rowGroup = new ArrayList<>();
            filteredRows.forEach(row -> rowGroup.add(row));
            rowGroups.add(rowGroup);
        }
        model.addAttribute("attributeNames", attributeNames);
        model.addAttribute("friendlyNames", friendlyNames);
        model.addAttribute("rowGroups", rowGroups);
        return "templates/read-list";
    }

    public JpaRepository getRepo(String templateType) throws IllegalTemplateException {
        switch (templateType) {
        case "ClientProfileTemplate":
        case "clientProfile":
            return clientProfileTemplateRepository;
        case "NARsTemplate":
        case "NARs":
            return narsTemplateRepository;
        }
        throw new IllegalTemplateException(String.format("invalid template: %s", templateType));
    }

}