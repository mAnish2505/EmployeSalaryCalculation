package com.example.SalaryCalculation;
import lombok.Data;
import lombok.Generated;
@Data
@Generated
public class MonthlyPayment {
    private int emp_id;
    private String name;
    private int age;
    private String month;
    private int year;
    private int total_month_days;
    private int days_paid;
    private int leaves;
    private double gross_Salary;
    private double leaves_Amount;
    private double tax;
    private double net_Salary;
    }
