package com.example.SalaryCalculation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Month;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
@Component
public class EmployeeMonthlyAtte {
    private static Map<String, MonthlyAttend> monthlyAttendHashMap = new LinkedHashMap<>();
    private static Map<Integer, AnnualSalary> annualSalaryHashMap = new HashMap<>();
    private static Map<String, MonthlyPayment> monthlyPaymentHashMap = new LinkedHashMap<>();
    @Value("${app.totalLeave}")
    private int totalLeave;
    @Value("${app.tax}")
    private double tax;

    public void readCsv() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader("E:\\Intellij_Project\\IntelliJ IDEA Community Edition 2021.1.1\\PracticeFiles\\EmployeeMonthlyAttendance.csv"))){
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                MonthlyAttend em = new MonthlyAttend();
                String[] values = line.split(",");
                String key=values[0]+"-"+values[5]+"-"+values[6];
                em.setEmployeeId(Integer.parseInt(values[0]));
                em.setEmployeeName(values[1]);
                em.setEmployeeAge(Integer.parseInt(values[2]));
                em.setWorkingDays(Integer.parseInt(values[3]));
                em.setLeaves(Integer.parseInt(values[4]));
                em.setMonth(values[5]);
                em.setYear(Integer.parseInt(values[6]));
                monthlyAttendHashMap.put(key, em);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(monthlyAttendHashMap);

        try (BufferedReader br = new BufferedReader(new FileReader("E:\\Intellij_Project\\IntelliJ IDEA Community Edition 2021.1.1\\PracticeFiles\\EmployeeAnnualSalary.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                AnnualSalary emp = new AnnualSalary();
                String[] values = line.split(",");
                emp.setEmp_ID(Integer.parseInt(values[0]));
                emp.setEmp_Salary(Double.parseDouble(values[1]));
                annualSalaryHashMap.put(Integer.parseInt(values[0]), emp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(annualSalaryHashMap);
        calculate_Salary();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void writeCsv() throws Exception {
        readCsv();
        String path = "E:\\Intellij_Project\\IntelliJ IDEA Community Edition 2021.1.1\\PracticeFiles\\EmployeeMonthlySalary.csv";
        try (FileWriter writer = new FileWriter(path, false)) {
            writer.write("EmpId,Name,Age,Month,Year,TotalDays,WorkingDays,Leaves,GrossSalary,LeavesDeduction,TaxDeducted,NetSalary\r\n");
            for (Map.Entry<String, MonthlyPayment> entry : monthlyPaymentHashMap.entrySet()) {
                MonthlyPayment emp = entry.getValue();
                writer.write(String.valueOf(emp.getEmp_id()));
                writer.write(",");
                writer.write(emp.getName());
                writer.write(",");
                writer.write(String.valueOf(emp.getAge()));
                writer.write(",");
                writer.write(String.valueOf(emp.getMonth()));
                writer.write(",");
                writer.write(String.valueOf(emp.getYear()));
                writer.write(",");
                writer.write(String.valueOf(emp.getTotal_month_days()));
                writer.write(",");
                writer.write(String.valueOf(emp.getDays_paid()));
                writer.write(",");
                writer.write(String.valueOf(emp.getLeaves()));
                writer.write(",");
                writer.write(String.valueOf(emp.getGross_Salary()));
                writer.write(",");
                writer.write(String.valueOf(emp.getLeaves_Amount()));
                writer.write(",");
                writer.write(String.valueOf(emp.getTax()));
                writer.write(",");
                writer.write(String.valueOf(emp.getNet_Salary()));
                writer.write("\r\n");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void calculate_Salary() throws Exception {
        Map.Entry<String,MonthlyAttend> firstKey = monthlyAttendHashMap.entrySet().iterator().next();
        int empId = firstKey.getValue().getEmployeeId();
        int  sumOfLeaves= 0;
        for (Map.Entry<String, MonthlyAttend> entry : monthlyAttendHashMap.entrySet()) {
            String key = entry.getKey();
            MonthlyAttend attend = entry.getValue();
            int monthDays= setData(attend);
            int totalDays =Leap(attend.getYear());
            double perdaysalary = annualSalaryHashMap.get(attend.getEmployeeId()).getEmp_Salary() / totalDays;
            double gross_Salary = Math.round(perdaysalary * attend.getWorkingDays());
            double leaves_deduction=0.0;

            MonthlyPayment ms = new MonthlyPayment();
            ms.setGross_Salary(gross_Salary);
            tax=taxCalculation(attend.getEmployeeAge(),gross_Salary);
            if (attend.getEmployeeId() != empId) {
                sumOfLeaves = 0;
            }
            for (int i = 0,j = 1; i < attend.getLeaves(); i++){
                        sumOfLeaves = sumOfLeaves + j;
                    if (sumOfLeaves > totalLeave) {
                       //  System.out.println(sumOfLeaves+" "+totalLeave);
                         leaves_deduction += perdaysalary;
                 }
            }
            double net_Salary = Math.round(gross_Salary - leaves_deduction - tax);
            ms.setEmp_id(attend.getEmployeeId());
            ms.setName(attend.getEmployeeName());
            ms.setAge(attend.getEmployeeAge());
            ms.setMonth(attend.getMonth());
            ms.setYear(attend.getYear());
            ms.setTotal_month_days((monthDays));
            ms.setDays_paid(attend.getWorkingDays());
            ms.setLeaves(attend.getLeaves());
            ms.setLeaves_Amount(leaves_deduction);
            ms.setTax(tax);
            ms.setNet_Salary(net_Salary);
            monthlyPaymentHashMap.put(key, ms);
            empId=attend.getEmployeeId();
        }
        System.out.println(monthlyPaymentHashMap);
    }

    public int setData(MonthlyAttend attend )throws Exception{
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH);
            int month = Month.valueOf(attend.getMonth().toUpperCase()).getValue();
            int month_days = days_of_month(attend.getYear(),month);
            if (attend.getYear() > currentYear || (month > currentMonth && attend.getYear() == currentYear)) {
                throw new Exception("Future record! Date exceeds current date");
            }
            if ((attend.getWorkingDays() + attend.getLeaves()) != month_days) {
                throw new Exception("Invalid Month");
            }
            return month_days;
        }

    public static int Leap(int year) {
        if(((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)){
            return 366; }
        else {
            return 365; }
    }

    public int days_of_month(int year,int month) {
        YearMonth yearMonthObject = YearMonth.of(year,month);
        return yearMonthObject.lengthOfMonth();
    }
    public double taxCalculation(int age,double monthlySalary)
    {
        double tax=0.0;
        if (age<= 25)
             tax = (monthlySalary * 20 / 100);
        else if ((age > 25) && (age < 50))
                tax =  (monthlySalary* 35 / 100);
        else if ((age >= 50) && (age < 60))
                tax = (monthlySalary * 30 / 100);
        else if (age >= 60)
                tax = 0;
        return tax;
    }
}


