package com.example.analytics.repository;

public final class AdscribeQueryConstants {

    private AdscribeQueryConstants() {
    }

    public static final String ADSCRIBE_KPI_QUERY = """
            SELECT
                SUM(revenue) AS total_revenue,
                SUM(orders) AS total_orders,
                SUM(impressions) AS total_impressions,
                AVG(revenue_per_order) AS avg_revenue_per_order,
                AVG(revenue_per_impression) AS avg_revenue_per_impression,
                AVG(impressions_per_order) AS avg_impressions_per_order
            FROM main_db.public.staged_adscribe_performance
            WHERE report_date BETWEEN ? AND ?
            LIMIT 50
            """;

    public static final String ADSCRIBE_DAILY_QUERY = """
            SELECT
                report_date AS date,
                SUM(revenue) AS revenue,
                SUM(orders) AS orders,
                SUM(impressions) AS impressions
            FROM main_db.public.staged_adscribe_performance
            WHERE report_date BETWEEN ? AND ?
            GROUP BY report_date
            ORDER BY report_date
            LIMIT 50
            """;

    public static final String ADSCRIBE_BY_CLIENT_QUERY = """
            SELECT
                client_name,
                SUM(revenue) AS revenue
            FROM main_db.public.staged_adscribe_performance
            WHERE report_date BETWEEN ? AND ?
            GROUP BY client_name
            ORDER BY client_name
            LIMIT 50
            """;

    public static final String ADSCRIBE_TOP_SHOWS_QUERY = """
            SELECT
                show_name,
                SUM(revenue) AS revenue
            FROM main_db.public.staged_adscribe_performance
            WHERE report_date BETWEEN ? AND ?
            GROUP BY show_name
            ORDER BY revenue DESC, show_name ASC
            LIMIT 5
            """;

    public static final String ADSCRIBE_DETAIL_QUERY = """
            SELECT
                report_date,
                client_name,
                show_name,
                revenue,
                orders,
                impressions,
                revenue_per_order,
                revenue_per_impression,
                impressions_per_order
            FROM main_db.public.staged_adscribe_performance
            WHERE report_date BETWEEN ? AND ?
            ORDER BY report_date, client_name, show_name
            LIMIT 50
            """;
}
