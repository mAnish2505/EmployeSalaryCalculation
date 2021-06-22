package com.example.SalaryCalculation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EmployeeSalaryCalculation
          {
                public static void main(String[] args) throws Exception {
		        SpringApplication.run(EmployeeSalaryCalculation.class, args);
		        EmployeeMonthlyAtte obj= new EmployeeMonthlyAtte();
		        obj.writeCsv();
                }

}
