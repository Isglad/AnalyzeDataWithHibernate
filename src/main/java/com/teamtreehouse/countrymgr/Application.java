package com.teamtreehouse.countrymgr;

import com.teamtreehouse.countrymgr.model.Country;
import com.teamtreehouse.countrymgr.model.Country.CountryBuilder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.*;

public class Application {
    // Hold a reusable reference to a SessionFactory (since we need only one)
    private static final SessionFactory sessionFactory = buildSessionFactory();
    private static final Scanner scanner = new Scanner(System.in);

    private static SessionFactory buildSessionFactory() {
        // Create a StandardServiceRegistry
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            displayMenu(); // Display the menu with options
            int choice = scanner.nextInt(); // Get user choice
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    createCountry();
                    break;
                case 2:
                    editCountry();
                    break;
                case 3:
                    deleteCountry();
                    break;
                case 4:
                    displayFormattedCountries();
                    break;
                case 5:
                    displayStatistics();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close(); // close the scanner when done
    }

    private static void displayMenu() {
        System.out.println("\n\nMenu:");
        System.out.println("1. Create a new country");
        System.out.println("2. Edit an existing country");
        System.out.println("3. Delete a country");
        System.out.println("4. View all countries");
        System.out.println("5. View statistics");
        System.out.println("6. Exit");
        System.out.print("\nEnter your choice: ");
    }

    private static Country fetchCountryByCode(String code) {
        // Open a session
        Session session = sessionFactory.openSession();

        // Retrieve the persistent object (or null if not found)
        Country country = session.get(Country.class,code);

        // Close the session
        session.close();

        // Return the object
        return country;
    }

    private static void delete(Country country) {
        // Open a session
        Session session = sessionFactory.openSession();

        // Begin a transaction
        session.beginTransaction();

        // Use the session to update the country
        session.delete(country);

        // Commit the transaction
        session.getTransaction().commit();

        // Close the session
        session.close();
    }

    private static void update(Country country) {
        // Open a session
        Session session = sessionFactory.openSession();

        // Begin a transaction
        session.beginTransaction();

        // Use the session to update the country
        session.update(country);

        // Commit the transaction
        session.getTransaction().commit();

        // Close the session
        session.close();
    }

    @SuppressWarnings("unchecked")
    private static List<Country> fetchAllCountries() {
        try (Session session = sessionFactory.openSession()) {
            // Create CriteriaBuilder
            CriteriaBuilder builder = session.getCriteriaBuilder();

            // Create CriteriaQuery
            CriteriaQuery<Country> criteria = builder.createQuery(Country.class);

            // Specify the root entity
            Root<Country> root = criteria.from(Country.class);

            // Select all countries
            criteria.select(root);

            // Execute the query
            List<Country> countries = session.createQuery(criteria).getResultList();

            return countries;
        }
    }

    private static void displayFormattedCountries() {
        List<Country> countries = fetchAllCountries();

        // Print the header
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("                                 COUNTRY DATA                                     ");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-10s %-30s %-20s %-20s%n", "CODE", "NAME", "INTERNET USERS (%)", "ADULT LITERACY RATE (%)");
        System.out.println("----------------------------------------------------------------------------------");

        // Print each country in a formatted row
        for (Country country : countries) {
            String internetUsers = country.getInternetUsers() != null
                    ? String.format("%.2f", country.getInternetUsers())
                    : "--";

            String adultLiteracyRate = country.getAdultLiteracyRate() != null
                    ? String.format("%.2f", country.getAdultLiteracyRate())
                    : "--";

            System.out.printf("%-10s %-30s %-20s %-20s%n",
                    country.getCode(),
                    country.getName(),
                    internetUsers,
                    adultLiteracyRate);
        }
    }

    private static void displayStatistics() {
        List<Country> countries = fetchAllCountries();
        // Calculate stats for Internet Users
        Optional<Country> maxInternetUsers = countries.stream() // use stream() to process the list of countries
                .filter(c -> c.getInternetUsers() != null) // use filter() to ensure null values are skipped before calculating the statistics
                .max(Comparator.comparing(Country::getInternetUsers));

        Optional<Country> minInternetUsers = countries.stream() // use Optional to avoid exceptions and check if the result is present before displaying.
                .filter(c -> c.getInternetUsers() != null)
                .min(Comparator.comparing(Country::getInternetUsers));
        // Calculate average for Internet Users
        OptionalDouble avgInternetUsers = countries.stream()
                .filter(c -> c.getInternetUsers() != null)
                .mapToDouble(Country::getInternetUsers)
                .average();

        // Calculate stats for Adult Literacy Rate
        Optional<Country> maxLiteracyRate = countries.stream()
                .filter(c -> c.getAdultLiteracyRate() != null)
                .max(Comparator.comparing(Country::getAdultLiteracyRate));

        Optional<Country> minLiteracyRate = countries.stream()
                .filter(c -> c.getAdultLiteracyRate() != null)
                .min(Comparator.comparing(Country::getAdultLiteracyRate));
        // Calculate average for Internet Users
        OptionalDouble avgLiteracyRate = countries.stream()
                .filter(c -> c.getAdultLiteracyRate() != null)
                .mapToDouble(Country::getAdultLiteracyRate)
                .average();

        // Print Results
        System.out.println("\n========= Statistics =========\n");

        System.out.println("Internet Users (%):");
        if (maxInternetUsers.isPresent() && minInternetUsers.isPresent()) {
            System.out.printf(" Maximum: %s - %.2f%%%n",
                    maxInternetUsers.get().getName(), maxInternetUsers.get().getInternetUsers());

            System.out.printf(" Minimum: %s - %.2f%%%n",
                    minInternetUsers.get().getName(), minInternetUsers.get().getInternetUsers());
        } else {
            System.out.println(" No data available.");
        }

        if (avgInternetUsers.isPresent()) {
            System.out.printf(" Average: %.2f%%%n", avgInternetUsers.getAsDouble());
        } else {
            System.out.println(" Average: --");
        }

        System.out.println("\nAdult Literacy Rate (%):");
        if (maxLiteracyRate.isPresent() && minLiteracyRate.isPresent()) {
            System.out.printf(" Maximum: %s - %.2f%%%n",
                    maxLiteracyRate.get().getName(), maxLiteracyRate.get().getAdultLiteracyRate());

            System.out.printf(" Minimum: %s - %.2f%%%n",
                    minLiteracyRate.get().getName(), minLiteracyRate.get().getAdultLiteracyRate());
        } else {
            System.out.println(" No data available.");
        }

        if (avgLiteracyRate.isPresent()) {
            System.out.printf(" Average: %.2f%%%n", avgLiteracyRate.getAsDouble());
        } else {
            System.out.println(" Average: --");
        }
    }

    // Method to create a new country
    private static void createCountry() {
        String countryCode = getValidCountryCode();

        System.out.print("Enter country name: ");
        String countryName = scanner.nextLine().trim();

        Double internetUsers = getValidDoubleInput("Enter percentage of internet users (or leave blank if unknown): ");
        Double adultLiteracyRate = getValidDoubleInput("Enter percentage of adult literacy rate (or leave blank if unknown): ");

        // Now create and save the new country to the database
        Country country = new CountryBuilder(countryCode, countryName)
                .withInternetUsers(internetUsers)
                .withAdultLiteracyRate(adultLiteracyRate)
                .build();
        save(country);
        System.out.println("Country created successfully!");
    }


    // Method to edit an existing country's data
    private static void editCountry() {
        String countryCode = getValidCountryCode();

        // Fetch the existing country by code
        Country country = fetchCountryByCode(countryCode);

        if(country == null) {
            System.out.println("Country with code " + countryCode + " not found.");
            return;
        }

        // Display current country data
        System.out.println("Current data: ");
        displayFormattedCountries(); // Show current data for editing

        // Prompt user for new values
        System.out.print("Enter new country name (Current: " + country.getName() + "): ");
        String newCountryName = scanner.nextLine().trim();

        Double newInternetUsers = getValidDoubleInput("Enter new percentage of internet users (Current: " + country.getInternetUsers() + "): ");

        Double newAdultLiteracyRate = getValidDoubleInput("Enter new adult literacy rate (Current: " + country.getAdultLiteracyRate() + "): ");

        country.setName(newCountryName);
        country.setInternetUsers(newInternetUsers);
        country.setAdultLiteracyRate(newAdultLiteracyRate);

        update(country);
        System.out.println("Country updated successfully!");
    }

    // Method to remove countries from database
    private static void deleteCountry() {
        String countryCode = getValidCountryCode();
        Country country = fetchCountryByCode(countryCode);
        if(country != null) {
            delete(country);
            System.out.println("Country deleted.");
        } else {
            System.out.println("Country not found.");
        }
    }

    private static String getValidCountryCode() {
        String countryCode;
        while(true) {
            System.out.print("Enter country code (maximum 3 characters): ");
            countryCode = scanner.nextLine().trim().toUpperCase();

            // Validate the country code length
            if (countryCode.length() > 3) {
                System.out.println("Country code cannot exceed 3 characters. Please try again");
            } else if (countryCode.isEmpty()) {
                System.out.println("Country code cannot be empty. Please try again.");
            } else {
                break;
            }
        }
        return countryCode;
    }

    private static Double getValidDoubleInput(String prompt) {
        Double value = null;
        while(true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()){
                break; // Allow NULL values if the input is left blank
            }

            try {
                value = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number or leave bank if unknown.");
            }
        }
        return value;
    }

    private static void save(Country country) {
        // Open a session
       try (Session session = sessionFactory.openSession();){
           session.beginTransaction();  // Begin a transaction
           String code = (String) session.save(country); // Use the session to save the country
           session.getTransaction().commit();
       }
    }
}

// main
// displayMenu
// createCountry
// editCountry
// deleteCountry
// displayFormattedCountries
// displayStatistics
// getValidCountryCode
// getValidDoubleInput
// save
// update
// delete
// fetchCountByCode
// fetchAllCountries