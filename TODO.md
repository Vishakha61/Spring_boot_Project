# TODO: Microservices Refactor for Inventory Billing System

## Overview
Refactor the single-module console app into a multi-module Maven project with two microservices:
- **inventory-service**: Manages items and stock via REST APIs (port 8081).
- **billing-service**: Handles billing, sales, and web UI with Thymeleaf (port 8080). Calls inventory-service APIs.

Keep JPA/Hibernate, shared MySQL DB for simplicity. Use RestTemplate for inter-service communication.

## Completed Features
### ✅ User Authentication & Authorization
- **Login System**: Secure user login with Spring Security
- **Registration System**: User registration with validation
- **Session Management**: Automatic session handling and logout
- **Role-based Access**: Support for USER and ADMIN roles
- **Default Users**: 
  - Admin: username=`admin`, password=`admin123`
  - User: username=`user`, password=`user123`
- **Styled UI**: Professional login and registration pages
- **Security Features**: BCrypt password encryption, CSRF protection

## Steps

### Phase 1: Project Structure Setup
- [x] **Step 1**: Update root `pom.xml` to parent multi-module configuration (add modules: inventory-service, billing-service; inherit Spring Boot parent).
- [x] **Step 2**: Create `inventory-service` module directory and files (pom.xml, src/main/java/.../InventoryApplication.java, move Item.java, ItemRepository.java, InventoryService.java).
- [x] **Step 3**: Create `billing-service` module directory and files (pom.xml, src/main/java/.../BillingApplication.java, move Bill.java, BillItem.java, Sales.java, create BillRepository.java if missing, SalesRepository.java, BillingService.java).

### Phase 2: Add Web and API Features
- [x] **Step 4**: Update poms for web: Add `spring-boot-starter-web` to both; add `spring-boot-starter-thymeleaf` and `spring-webflux` (for RestTemplate) to billing-service.
- [x] **Step 5**: Create REST Controller in inventory-service (`ItemController.java`): Endpoints for GET/POST/PUT/DELETE /items.
- [x] **Step 6**: Modify InventoryService to support API responses (e.g., DTOs if needed, but keep simple).
- [x] **Step 7**: Create Controller in billing-service (`BillingController.java`): REST for /bills, /sales; Thymeleaf views for UI (menu, list items, generate bill, view sales).

### Phase 3: Inter-Service Communication and UI
- [x] **Step 8**: Update BillingService to use RestTemplate: Call inventory-service for item fetch/stock update (e.g., GET /items/{id}, PUT /items/{id}/stock).
- [x] **Step 9**: Add RestTemplate configuration bean in billing-service.
- [x] **Step 10**: Create Thymeleaf templates in billing-service (`src/main/resources/templates/`): index.html (menu), items.html (list), bill-form.html (generate bill), sales.html (report).

### Phase 4: Configuration and Cleanup
- [x] **Step 11**: Create application.yml for each service (split from properties): DB config, server.port, inventory-service URL in billing.
- [x] **Step 12**: Add authentication system with login and registration pages.
- [x] **Step 13**: Create missing repositories if needed (e.g., BillRepository extends JpaRepository<Bill, Integer>).

### Phase 5: Fix and Test "View Items" Functionality
- [x] **Step 14**: Add `getAllItems()` method to `BillingService.java` using RestTemplate to fetch items from inventory-service API (return List<Map<String, Object>> for simplicity).
- [x] **Step 15**: Update `BillingController.java` getItems() method to call `billingService.getAllItems()` and add "items" attribute to model.
- [x] **Step 16**: Create `items.html` template in billing-service templates/ to display items in a table using Thymeleaf (columns: ID, Name, Category, Price, Quantity).
- [ ] **Step 17**: Restart billing-service and test /items page (should show empty list if no items; add sample item via curl to inventory-service if needed).
- [ ] **Step 18**: Run `mvn clean install` in root to verify build.
- [ ] **Step 19**: Start services: Run inventory-service first, then billing-service.
- [ ] **Step 20**: Test: Access http://localhost:8080 (UI), verify API calls, DB updates, stock reduction on billing.

Track progress by updating this file after each step.

## Recent Additions
- [x] **Authentication**: Complete login/register system implemented
- [x] **User Management**: User entity, repository, and service created
- [x] **Security Configuration**: Spring Security with proper URL protection
- [x] **UI Enhancement**: Styled dashboard with user info and logout functionality
- [x] **Database**: User table with role support (USER/ADMIN)
- [x] **Session Handling**: Proper login/logout flow with redirects
