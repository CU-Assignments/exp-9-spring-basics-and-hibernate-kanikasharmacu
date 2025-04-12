// Account.java
package com.example.banking.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;
    
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    public Account() {
    }
    
    public Account(String accountNumber, String ownerName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balance +
                '}';
    }
}

// Transaction.java
package com.example.banking.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "from_account", nullable = false)
    private String fromAccount;
    
    @Column(name = "to_account", nullable = false)
    private String toAccount;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String status;
    
    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Transaction(String fromAccount, String toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFromAccount() {
        return fromAccount;
    }
    
    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }
    
    public String getToAccount() {
        return toAccount;
    }
    
    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}

// AppConfig.java
package com.example.banking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.example.banking")
@PropertySource("classpath:database.properties")
public class AppConfig {
    
    @Autowired
    private Environment environment;
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driverClassName"));
        dataSource.setUrl(environment.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(environment.getRequiredProperty("jdbc.password"));
        return dataSource;
    }
    
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.example.banking.entity");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }
    
    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
    
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
        properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getRequiredProperty("hibernate.format_sql"));
        properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("hibernate.hbm2ddl.auto"));
        return properties;
    }
}

// AccountRepository.java
package com.example.banking.repository;

import com.example.banking.entity.Account;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class AccountRepository {
    
    private final SessionFactory sessionFactory;
    
    @Autowired
    public AccountRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public void save(Account account) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(account);
    }
    
    public Account findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Account.class, id);
    }
    
    public Account findByAccountNumber(String accountNumber) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);
        cq.select(root).where(cb.equal(root.get("accountNumber"), accountNumber));
        TypedQuery<Account> query = session.createQuery(cq);
        return query.getSingleResult();
    }
    
    public List<Account> findAll() {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);
        cq.select(root);
        TypedQuery<Account> query = session.createQuery(cq);
        return query.getResultList();
    }
}

// TransactionRepository.java
package com.example.banking.repository;

import com.example.banking.entity.Transaction;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class TransactionRepository {
    
    private final SessionFactory sessionFactory;
    
    @Autowired
    public TransactionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public void save(Transaction transaction) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(transaction);
    }
    
    public Transaction findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Transaction.class, id);
    }
    
    public List<Transaction> findAll() {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
        Root<Transaction> root = cq.from(Transaction.class);
        cq.select(root).orderBy(cb.desc(root.get("timestamp")));
        TypedQuery<Transaction> query = session.createQuery(cq);
        return query.getResultList();
    }
}

// InsufficientFundsException.java
package com.example.banking.exception;

public class InsufficientFundsException extends Exception {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// AccountNotFoundException.java
package com.example.banking.exception;

public class AccountNotFoundException extends Exception {
    
    public AccountNotFoundException(String message) {
        super(message);
    }
}

// BankingService.java
package com.example.banking.service;

import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankingService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public BankingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Transactional
    public void createAccount(Account account) {
        accountRepository.save(account);
    }
    
    @Transactional(readOnly = true)
    public Account getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
        try {
            return accountRepository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new AccountNotFoundException("Account with number " + accountNumber + " not found");
        }
    }
    
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    @Transactional(rollbackFor = {InsufficientFundsException.class, AccountNotFoundException.class})
    public void transferMoney(String fromAccountNumber, String toAccountNumber, BigDecimal amount) 
            throws InsufficientFundsException, AccountNotFoundException {
        
        // Create and save the initial transaction record
        Transaction transaction = new Transaction(fromAccountNumber, toAccountNumber, amount);
        transactionRepository.save(transaction);
        
        try {
            // Retrieve the accounts
            Account fromAccount = getAccountByNumber(fromAccountNumber);
            Account toAccount = getAccountByNumber(toAccountNumber);
            
            // Check for sufficient funds
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                transaction.setStatus("FAILED - INSUFFICIENT FUNDS");
                transactionRepository.save(transaction);
                throw new InsufficientFundsException("Insufficient funds in account " + fromAccountNumber);
            }
            
            // Perform the transfer
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));
            
            // Save the updated accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            // Update transaction status
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            
        } catch (AccountNotFoundException | InsufficientFundsException e) {
            // Update transaction status if not already done
            if (transaction.getStatus().equals("PENDING")) {
                transaction.setStatus("FAILED - " + e.getMessage());
                transactionRepository.save(transaction);
            }
            throw e; // Re-throw to trigger rollback
        } catch (Exception e) {
            // Handle unexpected errors
            transaction.setStatus("FAILED - SYSTEM ERROR");
            transactionRepository.save(transaction);
            throw e; // Re-throw to trigger rollback
        }
    }
}

// Main.java
package com.example.banking;

import com.example.banking.config.AppConfig;
import com.example.banking.entity.Account;
import com.example.banking.entity.Transaction;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.service.BankingService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Initialize Spring context
        AnnotationConfigApplicationContext context = 
            new AnnotationConfigApplicationContext(AppConfig.class);
        
        // Get BankingService bean
        BankingService bankingService = context.getBean(BankingService.class);
        
        try {
            // Create sample accounts
            System.out.println("Creating sample accounts...");
            Account account1 = new Account("ACC001", "John Doe", new BigDecimal("1000.00"));
            Account account2 = new Account("ACC002", "Jane Smith", new BigDecimal("500.00"));
            Account account3 = new Account("ACC003", "Bob Johnson", new BigDecimal("50.00"));
            
            bankingService.createAccount(account1);
            bankingService.createAccount(account2);
            bankingService.createAccount(account3);
            
            // Display all accounts
            System.out.println("\n--- Initial Account Status ---");
            displayAccounts(bankingService.getAllAccounts());
            
            // Perform successful transaction
            System.out.println("\n--- Performing Successful Transaction ---");
            try {
                bankingService.transferMoney("ACC001", "ACC002", new BigDecimal("200.00"));
                System.out.println("Transaction completed successfully!");
            } catch (InsufficientFundsException | AccountNotFoundException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            }
            
            // Display updated accounts
            System.out.println("\n--- Account Status After Successful Transaction ---");
            displayAccounts(bankingService.getAllAccounts());
            
            // Attempt transaction with insufficient funds (should fail)
            System.out.println("\n--- Attempting Transaction with Insufficient Funds ---");
            try {
                bankingService.transferMoney("ACC003", "ACC001", new BigDecimal("500.00"));
                System.out.println("Transaction completed successfully!");
            } catch (InsufficientFundsException | AccountNotFoundException e) {
                System.out.println("Transaction failed as expected: " + e.getMessage());
            }
            
            // Display accounts after failed transaction
            System.out.println("\n--- Account Status After Failed Transaction ---");
            displayAccounts(bankingService.getAllAccounts());
            
            // Attempt transaction with non-existent account (should fail)
            System.out.println("\n--- Attempting Transaction with Non-existent Account ---");
            try {
                bankingService.transferMoney("ACC001", "ACC999", new BigDecimal("100.00"));
                System.out.println("Transaction completed successfully!");
            } catch (InsufficientFundsException | AccountNotFoundException e) {
                System.out.println("Transaction failed as expected: " + e.getMessage());
            }
            
            // Display transaction history
            System.out.println("\n--- Transaction History ---");
            displayTransactions(bankingService.getAllTransactions());
            
        } finally {
            // Close the Spring context
            context.close();
        }
    }
    
    private static void displayAccounts(List<Account> accounts) {
        for (Account account : accounts) {
            System.out.println(account);
        }
    }
    
    private static void displayTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }
}

// database.properties
jdbc.driverClassName=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/banking_db?createDatabaseIfNotExist=true&useSSL=false
jdbc.username=root
jdbc.password=password
hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.hbm2ddl.auto=create-drop

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spring-hibernate-banking</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <spring.version>5.3.25</spring.version>
        <hibernate.version>5.6.15.Final</hibernate.version>
        <mysql.version>8.0.32</mysql.version>
    </properties>

    <dependencies>
        <!-- Spring Core -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
        
        <!-- Spring ORM -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>${spring.version}</version>
        </dependency>
        
        <!-- Spring Transactions -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${spring.version}</version>
        </dependency>
        
        <!-- Hibernate Core -->
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>${hibernate.version}</version>
        </dependency>
        
        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>
    </dependencies>
</project>
