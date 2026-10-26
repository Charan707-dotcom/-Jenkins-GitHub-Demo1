SIMPLE BANKING SYSTEM - RUN GUIDE

1. Requirements:
   - JDK 22
   - Maven (or use the included Maven wrapper if added later)
   - MySQL Server

2. MySQL:
   Create a database named banking_system, or let MySQL create it through the JDBC URL if your user has permission.
   In src/main/resources/application.properties change:
       spring.datasource.username=root
       spring.datasource.password=root
   to your actual MySQL username/password.

3. Run:
   From the BankingSystem folder:
       mvn spring-boot:run

   Or:
       mvn clean package
       java -jar target/banking-system-0.0.1-SNAPSHOT.jar

4. Open:
   http://localhost:8080

5. User:
   Register -> Login -> Create account (18+) -> Deposit/Withdraw -> Apply for loan.
   Loan interest is calculated from the stored loan start date after bank approval.

6. Bank/Admin:
   http://localhost:8080/bank/login
   Username: admin
   Password: admin123

IMPORTANT:
This is an educational/demo banking application. Passwords are stored plainly and there is no production-grade authentication/security. Do not use it for real banking.
