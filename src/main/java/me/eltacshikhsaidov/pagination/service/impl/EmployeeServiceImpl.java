package me.eltacshikhsaidov.pagination.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import me.eltacshikhsaidov.pagination.model.Employee;
import me.eltacshikhsaidov.pagination.repository.EmployeeRepository;
import me.eltacshikhsaidov.pagination.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final AmazonS3 amazonS3;

    private final EmployeeRepository employeeRepository;

    @Value("${application.bucket.name}")
    private String bucketName;

    public EmployeeServiceImpl(AmazonS3 amazonS3, EmployeeRepository employeeRepository) {
        this.amazonS3 = amazonS3;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        Optional<Employee> optional = employeeRepository.findById(id);
        Employee employee = null;

        if (optional.isPresent()) {
            employee = optional.get();
        } else {
            throw new RuntimeException("Employee not found for id :: " + id);
        }

        return employee;
    }

    @Override
    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public Page<Employee> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

        return employeeRepository.findAll(pageable);
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) {
        File file =  multipartFileToFile(multipartFile);
        String uniqueAddress = UUID.randomUUID().toString();
        String fileName = uniqueAddress + "-" + multipartFile.getOriginalFilename();
        amazonS3.putObject(bucketName,fileName,file);
        file.delete();
        return fileName;
    }

    @Override
    public File multipartFileToFile(MultipartFile multipartFile) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
            fileOutputStream.write(multipartFile.getBytes());
        }
        catch (IOException exception){
            System.out.println(exception.getMessage());
        }
        return file;
    }

    @Override
    public void getFile(String fileName, HttpServletResponse response) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        InputStream inputStream =  s3Object.getObjectContent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length;
        byte [] buffer = new byte[4096];
        while ((length=inputStream.read(buffer,0,buffer.length))!=-1){
            outputStream.write(buffer,0,length);
        }
        byte[] byteArray = new  byte[outputStream.size()];
        int i=0;
        for(Byte b : outputStream.toByteArray()){
            byteArray[i++]=b;
        }
        response.setContentType("image/jpeg");
        InputStream in = new ByteArrayInputStream(byteArray);
        IOUtils.copy(in,response.getOutputStream());
    }
}
