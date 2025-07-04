
import java.time.LocalDate;
import java.util.*;

public class FawryEcommerce {

    public static void main(String[] args) {
        Product cheese = new ExpirableShippableProduct("Cheese", 100, 10, LocalDate.now().plusDays(3), 200);
        Product biscuits = new ExpirableShippableProduct("Biscuits", 150, 5, LocalDate.now().plusDays(1), 700);
        Product tv = new ShippableProduct("TV", 300, 3, 5000);
        Product scratchCard = new Product("ScratchCard", 50, 10) {};

        Customer customer = new Customer("Shorouk", 1000);
        ShoppingCart cart = new ShoppingCart();

        cart.addToCart(cheese, 2);
        cart.addToCart(biscuits, 1);
        cart.addToCart(scratchCard, 1);

        cart.processCheckout(customer);
    }
}

abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int qty) {
        this.name = name;
        this.price = price;
        this.quantity = qty;
    }

    public boolean isAvailable(int requestedQty) {
        return quantity >= requestedQty;
    }

    public boolean isExpired() {
        return false;
    }

    public boolean needsShipping() {
        return false;
    }

    public double getWeight() {
        return 0;
    }

    public void reduceQty(int amount) {
        quantity -= amount;
    }

    public String getName() { return name; }

    public double getPrice() { return price; }
}

interface Shippable {
    String getName();
    double getWeight();
}

class ExpirableProduct extends Product {
    private LocalDate expiryDate;

    public ExpirableProduct(String name, double price, int qty, LocalDate expiryDate) {
        super(name, price, qty);
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}

class ShippableProduct extends Product implements Shippable {
    private double weight;

    public ShippableProduct(String name, double price, int qty, double weight) {
        super(name, price, qty);
        this.weight = weight;
    }

    @Override
    public boolean needsShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class ExpirableShippableProduct extends Product implements Shippable {
    private LocalDate expiryDate;
    private double weight;

    public ExpirableShippableProduct(String name, double price, int qty, LocalDate expiryDate, double weight) {
        super(name, price, qty);
        this.expiryDate = expiryDate;
        this.weight = weight;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    @Override
    public boolean needsShipping() {
        return true;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() { return balance; }

    public void charge(double amount) {
        balance -= amount;
    }

    public String getName() { return name; }
}

class ItemInCart {
    Product product;
    int qty;

    public ItemInCart(Product product, int qty) {
        this.product = product;
        this.qty = qty;
    }

    public double total() {
        return product.getPrice() * qty;
    }
}

class ShoppingCart {
    List<ItemInCart> items = new ArrayList<>();

    public void addToCart(Product product, int qty) {
        if (!product.isAvailable(qty)) {
            throw new RuntimeException("Requested quantity is not available.");
        }
        items.add(new ItemInCart(product, qty));
    }

    public void processCheckout(Customer customer) {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        double subtotalAmount = 0;
        double shippingCost = 0;
        List<Shippable> shippingList = new ArrayList<>();

        for (ItemInCart item : items) {
            if (item.product.isExpired()) {
                throw new IllegalStateException("Product is expired: " + item.product.getName());
            }

            subtotalAmount += item.total();

            if (item.product.needsShipping()) {
                shippingList.add((Shippable) item.product);
                shippingCost += 10;
            }
        }

        double finalAmount = subtotalAmount + shippingCost;

        if (customer.getBalance() < finalAmount) {
            throw new IllegalStateException("Insufficient balance.");
        }

        // شحن
        if (!shippingList.isEmpty()) {
            System.out.println("** Shipment notice **");
            double totalWeight = 0;
            for (ItemInCart item : items) {
                if (item.product.needsShipping()) {
                    double weight = item.product.getWeight() * item.qty;
                    System.out.println(item.qty + "x " + item.product.getName() + " " + weight + "g");
                    totalWeight += weight;
                }
            }
            System.out.printf("Total package weight %.1fkg\n", totalWeight / 1000);
        }

        // Checkout receipt
        System.out.println("** Checkout receipt **");
        for (ItemInCart item : items) {
            System.out.println(item.qty + "x " + item.product.getName() + " " + item.total());
            item.product.reduceQty(item.qty);
        }
        System.out.println("--------------------");
        System.out.println("Subtotal: " + subtotalAmount);
        System.out.println("Shipping: " + shippingCost);
        System.out.println("Total amount paid: " + finalAmount);
        customer.charge(finalAmount);
        System.out.println("Customer balance after payment: " + customer.getBalance());
    }
}
