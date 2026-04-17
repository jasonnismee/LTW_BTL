package com.restaurant.util;

import com.restaurant.model.entity.Category;
import com.restaurant.model.entity.MenuItem;
import com.restaurant.model.entity.RestaurantTable;
import com.restaurant.model.entity.User;
import com.restaurant.model.entity.Voucher;
import com.restaurant.model.enums.MenuItemStatus;
import com.restaurant.model.enums.TableStatus;
import com.restaurant.model.enums.UserRank;
import com.restaurant.model.enums.UserRole;
import com.restaurant.model.enums.UserStatus;
import com.restaurant.model.enums.VoucherType;
import com.restaurant.repository.CategoryRepository;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.RestaurantTableRepository;
import com.restaurant.repository.UserRepository;
import com.restaurant.repository.VoucherRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final MenuItemRepository menuItemRepository;
  private final RestaurantTableRepository tableRepository;
  private final VoucherRepository voucherRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeeder(UserRepository userRepository,
                    CategoryRepository categoryRepository,
                    MenuItemRepository menuItemRepository,
                    RestaurantTableRepository tableRepository,
                    VoucherRepository voucherRepository,
                    PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.menuItemRepository = menuItemRepository;
    this.tableRepository = tableRepository;
    this.voucherRepository = voucherRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (userRepository.count() > 0) {
      return;
    }

    String encoded = passwordEncoder.encode("password123");

    seedUsers(encoded);
    List<Category> categories = seedCategories();
    seedMenuItems(categories);
    seedTables();
    seedVouchers();
  }

  private void seedUsers(String encoded) {
    userRepository.saveAll(List.of(
        buildUser("admin", encoded, UserRole.ADMIN, "Quản trị viên", "0912345678", null, BigDecimal.ZERO, UserRank.BRONZE),
        buildUser("staff1", encoded, UserRole.STAFF, "Nguyễn Văn A", "0912345679", null, BigDecimal.ZERO, UserRank.BRONZE),
        buildUser("staff2", encoded, UserRole.STAFF, "Trần Thị B", "0912345680", null, BigDecimal.ZERO, UserRank.BRONZE),
        buildUser("customer1", encoded, UserRole.CUSTOMER, "Lê Minh C", "0912345681", null, BigDecimal.ZERO, UserRank.BRONZE),
        buildUser("customer2", encoded, UserRole.CUSTOMER, "Phạm Duy D", "0912345682", null, new BigDecimal("6000000"), UserRank.SILVER),
        buildUser("customer3", encoded, UserRole.CUSTOMER, "Hoàng E", "0912345683", null, new BigDecimal("16000000"), UserRank.GOLD),
        buildUser("customer4", encoded, UserRole.CUSTOMER, "Võ F", "0912345684", null, new BigDecimal("31000000"), UserRank.DIAMOND),
        buildUser("customer5", encoded, UserRole.CUSTOMER, "Đặng G", "0912345685", null, new BigDecimal("500000"), UserRank.BRONZE)
    ));
  }

  private User buildUser(String username, String encodedPassword, UserRole role, String fullName,
                         String phone, String email, BigDecimal totalSpending, UserRank rank) {
    User u = new User();
    u.setUsername(username);
    u.setPassword(encodedPassword);
    u.setRole(role);
    u.setFullName(fullName);
    u.setPhone(phone);
    u.setEmail(email);
    u.setTotalSpending(totalSpending);
    u.setRank(rank);
    u.setStatus(UserStatus.ACTIVE);
    return u;
  }

  private List<Category> seedCategories() {
    Category main = new Category();
    main.setName("Món Chính");
    main.setDescription("Cơm, mì, bún...");

    Category drink = new Category();
    drink.setName("Đồ Uống");
    drink.setDescription("Trà, cà phê, nước ép...");

    Category dessert = new Category();
    dessert.setName("Tráng Miệng");
    dessert.setDescription("Bánh, kem, chè...");

    Category appetizer = new Category();
    appetizer.setName("Khai Vị");
    appetizer.setDescription("Gỏi, salad, đồ ăn nhẹ...");

    return categoryRepository.saveAll(List.of(main, drink, dessert, appetizer));
  }

  private void seedMenuItems(List<Category> categories) {
    Category main = categories.stream().filter(c -> c.getName().equals("Món Chính")).findFirst().orElseThrow();
    Category drink = categories.stream().filter(c -> c.getName().equals("Đồ Uống")).findFirst().orElseThrow();
    Category dessert = categories.stream().filter(c -> c.getName().equals("Tráng Miệng")).findFirst().orElseThrow();
    Category appetizer = categories.stream().filter(c -> c.getName().equals("Khai Vị")).findFirst().orElseThrow();

    List<MenuItem> items = List.of(
        menuItem("Cơm gà xối mỡ", "65000", main),
        menuItem("Bún bò Huế", "75000", main),
        menuItem("Phở bò tái", "70000", main),
        menuItem("Mì Quảng", "68000", main),
        menuItem("Cơm tấm sườn bì chả", "72000", main),

        menuItem("Trà đá", "10000", drink),
        menuItem("Cà phê sữa", "25000", drink),
        menuItem("Nước ép cam", "30000", drink),
        menuItem("Trà chanh", "20000", drink),
        menuItem("Sinh tố bơ", "40000", drink),

        menuItem("Chè ba màu", "25000", dessert),
        menuItem("Kem Flan", "30000", dessert),
        menuItem("Bánh flan", "28000", dessert),
        menuItem("Chè đậu đỏ", "22000", dessert),
        menuItem("Phô mai nướng", "35000", dessert),

        menuItem("Gỏi cuốn", "35000", appetizer),
        menuItem("Nem nướng", "45000", appetizer),
        menuItem("Salad rau trộn", "40000", appetizer),
        menuItem("Đùi gà chiên", "55000", appetizer),
        menuItem("Tôm chiên giòn", "60000", appetizer)
    );
    menuItemRepository.saveAll(items);
  }

  private MenuItem menuItem(String name, String priceVnd, Category category) {
    MenuItem item = new MenuItem();
    item.setName(name);
    item.setPrice(new BigDecimal(priceVnd));
    item.setDescription(null);
    item.setImageUrl(null);
    item.setStatus(MenuItemStatus.AVAILABLE);
    item.setCategory(category);
    return item;
  }

  private void seedTables() {
    for (int i = 1; i <= 10; i++) {
      RestaurantTable t = new RestaurantTable();
      t.setTableNumber(i);
      t.setCapacity(4);
      t.setStatus(TableStatus.EMPTY);
      t.setPosition("Tầng 1");
      tableRepository.save(t);
    }
  }

  private void seedVouchers() {
    LocalDateTime now = LocalDateTime.now();

    Voucher v1 = new Voucher();
    v1.setCode("WELCOME10");
    v1.setDiscountPercent(10);
    v1.setType(VoucherType.ONLINE_RANK_ONLY);
    v1.setMinRank(UserRank.BRONZE);
    v1.setMinOrderAmount(new BigDecimal("100000"));
    v1.setMaxDiscountAmount(new BigDecimal("30000"));
    v1.setQuantity(-1);
    v1.setUsedCount(0);
    v1.setStartDate(now);
    v1.setEndDate(now.plusDays(30));
    v1.setStatus(true);

    Voucher v2 = new Voucher();
    v2.setCode("SILVER20");
    v2.setDiscountPercent(20);
    v2.setType(VoucherType.ONLINE_RANK_ONLY);
    v2.setMinRank(UserRank.SILVER);
    v2.setMinOrderAmount(new BigDecimal("150000"));
    v2.setMaxDiscountAmount(new BigDecimal("50000"));
    v2.setQuantity(50);
    v2.setUsedCount(0);
    v2.setStartDate(now);
    v2.setEndDate(now.plusDays(30));
    v2.setStatus(true);

    Voucher v3 = new Voucher();
    v3.setCode("STAFFOFF50");
    v3.setDiscountPercent(50);
    v3.setType(VoucherType.OFFLINE_ALL);
    v3.setMinRank(null);
    v3.setMinOrderAmount(new BigDecimal("200000"));
    v3.setMaxDiscountAmount(new BigDecimal("100000"));
    v3.setQuantity(20);
    v3.setUsedCount(0);
    v3.setStartDate(now);
    v3.setEndDate(now.plusDays(60));
    v3.setStatus(true);

    voucherRepository.saveAll(List.of(v1, v2, v3));
  }
}

