# BankingApp

## Overview
The **BankingApp** is a Java-based text banking application designed to manage customer accounts and perform banking operations. It uses an H2 database to store user and account data securely. Key features include user account management, deposits, withdrawals, loan applications, and administrative operations.

---

## Features
- **User Account Management:**
  - Add new customers with unique usernames.
  - Retrieve existing usernames and accounts.
  - Verify account existence for login operations.

- **Banking Operations:**
  - Deposit funds into accounts.
  - Withdraw funds with validation for sufficient balance.
  - Apply for loans and update loan amounts.

- **Administrative Features:**
  - View all usernames.
  - View all bank accounts.
  - Reset the database with truncation of all user and account data.

- **Database Management:**
  - H2 database integration with auto-generated tables (`users` and `accounts`).
  - Support for starting and stopping the H2 console server.

---

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or later.
- IntelliJ IDEA or another Java IDE.
- Maven for dependency management.

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-repository/BankingApp.git
2. Ensure that the H2 database server is set up correctly. The app will automatically start the H2 server and the console for you.
3. Build and run the application. It will start the H2 database server and open the web console at `http://localhost:8082` for administrative access.
   
## Database Schema

### Users Table 
| Column Name | Type | Constraints |
|:-----------:|:----:|:-----------:|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| first_name | VARCHAR(255) | NOT NULL | 
| last_name | VARCHAR(255) | NOT NULL |
| username | VARCHAR(255) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | NOT NULL |

### Accounts Table
| Column Name | Type | Constraints |
|:-----------:|:----:|:-----------:|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| account_number  | VARCHAR(20) | UNIQUE, NOT NULL |
| user_id | INT | FOREIGN KEY(users.id), NOT NULL |
| fund_amount | DECIMAL(15,2) | NOT NULL |
| loan_amount | DECIMAL(15,2) | DEFAULT 0 |

## Database Server
- **TCP Server**: Runs on port `9092` by default
- **Web Console**: Accessible at `http://localhost:8082` for administrative tasks like querying and managing the databbase.

## Troubleshooting
- Ensure that your database path is correct and accessible.
- If the application fails to start the H2 server, verify that port `9092` is available.
- If you're facing issues with connection or data integrity, check the H2 console for logs and database status.

## License
This project is licensed under the [MIT License](https://mit-license.org/).
