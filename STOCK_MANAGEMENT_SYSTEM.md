# 📦🔄 **Automatic Stock Management System**

## 🎯 **Overview**
When you generate a bill or sell an item, the stock is automatically reduced in the inventory. This ensures real-time inventory management across your microservices architecture.

## ✨ **Key Features**

### 🚀 **Automatic Stock Reduction**
- ✅ **Real-time updates**: Stock automatically reduces when bills are generated
- ✅ **Stock validation**: Prevents overselling with availability checks
- ✅ **Cross-service sync**: Billing service communicates with inventory service
- ✅ **Fallback mechanism**: Works even if inventory service is temporarily down

### 🔄 **Stock Operations**
- ✅ **Stock checking**: Verify availability before processing sales
- ✅ **Stock restoration**: Restore stock for cancelled sales
- ✅ **Manual stock adjustment**: Add or reduce stock manually
- ✅ **Transaction safety**: Atomic operations to prevent data inconsistency

## 🔧 **Technical Implementation**

### **Service Communication Flow**
```
Billing Service → Feign Client → Inventory Service
    (8082)                          (8081)
      ↓
  Generate Bill → Check Stock → Reduce Stock
      ↓
  Save Sale Record → Update Inventory → Sync to Products
```

### **Stock Validation Process**
1. **Pre-check**: Verify item exists and has sufficient stock
2. **Reserve**: Lock stock during transaction
3. **Deduct**: Reduce stock quantity after successful sale
4. **Sync**: Update product service with new stock levels

## 📝 **API Endpoints**

### **Automatic Stock Management** (Built-in)
```http
POST /generate-bill
Content-Type: application/x-www-form-urlencoded

itemId=1&quantity=5
```
**Result**: 
- ✅ Bill generated
- ✅ Stock automatically reduced by 5 units
- ✅ Sale recorded in database
- ✅ Inventory updated across services

### **Check Stock Availability**
```http
GET /api/items/{itemId}/stock/status
```
**Response**:
```json
{
  "itemId": 1,
  "itemName": "Laptop",
  "currentStock": 15,
  "requiredQuantity": 1,
  "available": true,
  "status": "AVAILABLE"
}
```

### **Cancel Sale & Restore Stock**
```http
POST /api/sales/{saleId}/cancel
```
**Response**:
```json
{
  "status": "success",
  "message": "Sale cancelled and stock restored successfully"
}
```

### **Manual Stock Restoration**
```http
POST /api/items/{itemId}/stock/restore?quantity=10&reason=Damaged items returned
```
**Response**:
```json
{
  "status": "success",
  "message": "Stock restored successfully",
  "itemId": 1,
  "quantity": 10,
  "reason": "Damaged items returned"
}
```

## 🎯 **Usage Examples**

### **Example 1: Generate Bill (Auto Stock Reduction)**
```bash
# Generate bill for 3 laptops
curl -X POST http://localhost:8082/generate-bill \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "itemId=1&quantity=3"
```

**What happens**:
1. ✅ System checks if 3 laptops are available
2. ✅ Stock validated: Available: 15, Required: 3 ✓
3. ✅ Bill generated for customer
4. ✅ Stock reduced: 15 → 12
5. ✅ Sale recorded in billing database
6. ✅ Inventory service updated
7. ✅ Product service synced

### **Example 2: Handle Insufficient Stock**
```bash
# Try to sell 20 laptops when only 12 available
curl -X POST http://localhost:8082/generate-bill \
  -d "itemId=1&quantity=20"
```

**Result**: 
```
❌ Error: "Insufficient stock! Available: 12, Required: 20"
✅ No stock deducted (transaction rolled back)
```

### **Example 3: Cancel Sale & Restore Stock**
```bash
# Cancel sale ID 5 and restore stock
curl -X POST http://localhost:8082/api/sales/5/cancel
```

**What happens**:
1. ✅ Sale record retrieved
2. ✅ Original item identified
3. ✅ Stock restored to inventory
4. ✅ Sale record removed
5. ✅ Customer refund processed

## 🔍 **Stock Management Features**

### **Stock Checking**
```bash
# Check stock for item ID 1
curl http://localhost:8081/api/items/1/stock/check?requiredQuantity=5
```

### **Manual Stock Addition**
```bash
# Add 50 units to inventory
curl -X PUT http://localhost:8081/api/items/1/stock/add?quantity=50
```

### **Stock Reduction (Direct)**
```bash
# Reduce 10 units from inventory
curl -X PUT http://localhost:8081/api/items/1/stock?quantity=10
```

## 🛡️ **Error Handling & Validation**

### **Stock Validation Rules**
- ❌ Cannot sell more than available stock
- ❌ Cannot sell negative quantities
- ❌ Cannot sell non-existent items
- ✅ Stock updates are atomic (all-or-nothing)

### **Failure Scenarios**
| Scenario | Behavior | Recovery |
|----------|----------|----------|
| **Inventory Service Down** | Use local product service | Manual sync later |
| **Insufficient Stock** | Reject sale immediately | Show available quantity |
| **Network Timeout** | Circuit breaker activates | Retry mechanism |
| **Transaction Failure** | Rollback stock changes | Log for manual review |

## 📊 **Stock Monitoring**

### **Real-time Stock Status**
```http
GET /api/items/{itemId}/stock/status
```
Shows:
- Current stock level
- Recent stock movements
- Availability status
- Low stock warnings

### **Stock Movement Logs**
```
📈 Stock added for item Laptop: 50 units (total: 65)
📉 Stock reduced for item Laptop: 3 units (remaining: 62)
🔄 Stock restored: 2 units for item ID: 1 (Sale cancellation)
```

## 🎊 **Benefits**

1. **🔄 Real-time Accuracy**: Stock levels always current
2. **🛡️ Prevents Overselling**: Cannot sell unavailable items
3. **📊 Automated Tracking**: No manual stock management needed
4. **🔄 Reversible Operations**: Cancel sales and restore stock
5. **🌐 Cross-service Sync**: All services have consistent data
6. **⚡ Fast Performance**: Optimized with circuit breakers
7. **📝 Complete Audit Trail**: Every stock change is logged

## ⚡ **Quick Test Workflow**

1. **Check Initial Stock**:
   ```bash
   curl http://localhost:8081/api/items/1
   ```

2. **Generate Sale**:
   ```bash
   curl -X POST http://localhost:8082/generate-bill -d "itemId=1&quantity=2"
   ```

3. **Verify Stock Reduced**:
   ```bash
   curl http://localhost:8081/api/items/1
   # Stock should be reduced by 2
   ```

4. **Check Sale Record**:
   ```bash
   curl http://localhost:8082/sales
   # New sale should appear in list
   ```

Your billing and inventory systems now work together seamlessly for complete stock management! 🎯

## 🔧 **Advanced Features**

### **Bulk Operations**
- Multiple item sales in single transaction
- Batch stock adjustments
- Bulk cancellations

### **Stock Alerts**
- Low stock notifications
- Reorder point management
- Stock movement reports

### **Integration Points**
- Product service synchronization
- Real-time dashboard updates
- Mobile app compatibility

Your stock management is now fully automated and bulletproof! 🚀