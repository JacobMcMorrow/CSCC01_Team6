package team6;

import team6.Organization;
import org.springframework.data.repository.CrudRepository;

//This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
//CRUD refers Create, Read, Update, Delete

public interface OrganizationRepository extends CrudRepository<Organization, Integer> {

}
