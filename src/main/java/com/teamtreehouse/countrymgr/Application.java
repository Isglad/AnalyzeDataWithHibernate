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

import java.util.List;

public class Application {
    // Hold a reusable reference to a SessionFactory (since we need only one)
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        // Create a StandardServiceRegistry
        final ServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
        return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    public static void main(String[] args) {
        // Check if the country with code "USA" exists
        Country existingCountry = findCountryByCode("USA");
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

        // Get the persisted country
        Country c = findCountryByCode(savedCountry);

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

        // Get the country with code of the USA
        c = findCountryByCode("USA");

        // Delete the country
        System.out.printf("%nDeleting...%n");
        delete(c);
        System.out.printf("%nDeleted!%n");
        System.out.printf("%nAfter delete%n");
//        fetchAllCountries().stream().forEach(System.out::println);
        displayFormattedCountries();
    }

    private static Country findCountryByCode(String code) {
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
