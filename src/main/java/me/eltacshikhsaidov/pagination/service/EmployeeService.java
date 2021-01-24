package me.eltacshikhsaidov.pagination.service;

import me.eltacshikhsaidov.pagination.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();
    void saveEmployee(Employee employee);
    Employee getEmployeeById(Long id);
    void deleteEmployeeById(Long id);
    Page<Employee> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection);

    String uploadImage(MultipartFile multipartFile);
    File multipartFileToFile(MultipartFile multipartFile);
    void getFile(String fileName, HttpServletResponse response) throws IOException;
}
