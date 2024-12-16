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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
        // Check if the country with code "USA" exists
        Country existingCountry = fetchCountryByCode("USA");
        if (existingCountry != null) {
            System.out.printf("%nCountry with code 'USA' already exists. Deleting the existing record...%n");
            delete(existingCountry);
        }
        // Now save the country
        Country country = new CountryBuilder("USA","United States")
                .withInternetUsers(46.2)
                .withAdultLiteracyRate(78.89)
                .build();
        String savedCountry = save(country);

        // Display a list of countries before the update
        System.out.printf("%n%nBefore update%n%n");
        //fetchAllCountries().stream().forEach(System.out::println);
        displayFormattedCountries();

        displayStatistics();

        // Get the persisted country
        Country c = fetchCountryByCode(savedCountry);

        // Update the country
        c.setName("United States of America");

        // Persist the changes
        System.out.printf("%nUpdating...%n");
        update(c);
        System.out.printf("%nUpdate complete!%n");

        // Display a list of countries after the update
        System.out.printf("%nAfter update%n");
        //fetchAllCountries().stream().forEach(System.out::println);
        displayFormattedCountries();

        displayStatistics();

        // Get the country with code of the USA
        c = fetchCountryByCode("USA");

        // Delete the country
        System.out.printf("%nDeleting...%n");
        delete(c);
        System.out.printf("%nDeleted!%n");
        System.out.printf("%nAfter delete%n");
//        fetchAllCountries().stream().forEach(System.out::println);
        displayFormattedCountries();

        displayStatistics();
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
        // Let's use stream() to process the list of countries
        // Let's use filter() to ensure null values are skipped before calculating the statistics
        // Since some values might be null,let's
        // use Optional to avoid exceptions and check if the result is present before displaying.
        Optional<Country> maxInternetUsers = countries.stream()
                .filter(c -> c.getInternetUsers() != null)
                .max(Comparator.comparing(Country::getInternetUsers));

        Optional<Country> minInternetUsers = countries.stream()
                .filter(c -> c.getInternetUsers() != null)
                .min(Comparator.comparing(Country::getInternetUsers));

        // Calculate stats for Adult Literacy Rate
        Optional<Country> maxLiteracyRate = countries.stream()
                .filter(c -> c.getAdultLiteracyRate() != null)
                .max(Comparator.comparing(Country::getAdultLiteracyRate));

        Optional<Country> minLiteracyRate = countries.stream()
                .filter(c -> c.getAdultLiteracyRate() != null)
                .min(Comparator.comparing(Country::getAdultLiteracyRate));

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

        System.out.println("\nAdult Literacy Rate (%):");
        if (maxLiteracyRate.isPresent() && minLiteracyRate.isPresent()) {
            System.out.printf(" Maximum: %s - %.2f%%%n",
                    maxLiteracyRate.get().getName(), maxLiteracyRate.get().getAdultLiteracyRate());

            System.out.printf(" Minimum: %s - %.2f%%%n",
                    minLiteracyRate.get().getName(), minLiteracyRate.get().getAdultLiteracyRate());
        } else {
            System.out.println(" No data available.");
        }
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

    private static String save(Country country) {
        // Open a session
        Session session = sessionFactory.openSession();

        // Begin a transaction
        session.beginTransaction();

        // Use the session to save the country
        String code = (String)session.save(country);

        // Commit the transaction
        session.getTransaction().commit();

        // Close the session
        session.close();

        return code;
    }
}
