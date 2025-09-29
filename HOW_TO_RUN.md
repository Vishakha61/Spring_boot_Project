# 🚀 How to Run Your Spring Boot Application

## Step-by-Step Instructions

### 1. Start the Applications

#### Option A: Using PowerShell (Recommended)

**Terminal 1 - Start Inventory Service (Port 8081):**
```powershell
cd "c:\spring boot\Spring_boot_Project"
mvnw.cmd spring-boot:run -pl inventory-service
```

**Terminal 2 - Start Billing Service (Port 8080):**
```powershell
cd "c:\spring boot\Spring_boot_Project"
mvnw.cmd spring-boot:run -pl billing-service
```

#### Option B: Using Command Prompt
```cmd
cd "c:\spring boot\Spring_boot_Project"
mvnw.cmd spring-boot:run -pl billing-service
```

### 2. Access in Browser

Once both services are running, open your web browser and navigate to:

🌐 **Main Application:** [http://localhost:8080](http://localhost:8080)

### 3. Available Pages & Features

| Page | URL | Description |
|------|-----|-------------|
| **Home Dashboard** | `http://localhost:8080/` | Main navigation and overview |
| **Items Management** | `http://localhost:8080/items` | View, edit, delete items |
| **Add New Item** | `http://localhost:8080/add-item` | Add new inventory items |
| **Edit Item** | `http://localhost:8080/edit-item/{id}` | Edit specific item |
| **Inventory Dashboard** | `http://localhost:8080/inventory` | Inventory section overview |
| **Billing Dashboard** | `http://localhost:8080/billing` | Billing section overview |
| **Generate Bill** | `http://localhost:8080/generate-bill` | Create sales transactions |
| **View Sales** | `http://localhost:8080/sales` | Sales history |
| **Sales Report** | `http://localhost:8080/report` | Sales analytics |

### 4. Login Information

The application includes authentication. Default login:
- **Username:** `admin` (check your UserService for exact credentials)
- **Password:** `admin` (check your UserService for exact credentials)

### 5. What You Can Do

✅ **Add Items:** Click "Add New Item" or navigate to `/add-item`
✅ **Edit Items:** Click "Edit" button next to any item in the items list
✅ **Delete Items:** Click "Delete" button with confirmation dialog
✅ **Monitor Stock:** View stock levels with color-coded indicators
✅ **Generate Bills:** Create sales transactions
✅ **View Reports:** Access sales analytics and reports

### 6. Troubleshooting

**If the application doesn't start:**
1. Make sure Java is installed: `java -version`
2. Ensure you're in the correct directory: `c:\spring boot\Spring_boot_Project`
3. Check if ports 8080 and 8081 are free
4. Look for error messages in the terminal

**If you get connection errors:**
- Make sure both services (billing-service on 8080 and inventory-service on 8081) are running

### 7. Development Mode

For development, you can run both services simultaneously in separate terminals. The billing service will automatically try to connect to the inventory service for full functionality.

**Services Architecture:**
- **Billing Service (Port 8080):** Web interface, authentication, billing logic
- **Inventory Service (Port 8081):** REST API for inventory management

---

**🎯 Quick Start:** Just run the billing service on port 8080 and access `http://localhost:8080` in your browser to start using all the item management features!