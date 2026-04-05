package com.example.analytics.repository;

public final class QueryConstants {

    private QueryConstants() {
    }

    public static final String KPI_QUERY = """
            SELECT
                COUNT(*) AS total_orders,
                SUM(revenue) AS total_revenue,
                AVG(revenue_per_order) AS avg_revenue_per_order
            FROM public.staged_processed_orders
            WHERE (? IS NULL OR client = ?)
            """;

    public static final String DAILY_ANALYTICS_QUERY = """
            SELECT
                CAST(order_date AS DATE) AS date,
                SUM(revenue) AS revenue,
                SUM(orders) AS orders
            FROM public.staged_processed_orders
            WHERE (? IS NULL OR client = ?)
            GROUP BY CAST(order_date AS DATE)
            ORDER BY CAST(order_date AS DATE)
            """;

    public static final String MONTHLY_ANALYTICS_QUERY = """
            SELECT
                DATE_TRUNC('month', order_date) AS month,
                SUM(revenue) AS revenue
            FROM public.staged_processed_orders
            WHERE (? IS NULL OR client = ?)
            GROUP BY DATE_TRUNC('month', order_date)
            ORDER BY DATE_TRUNC('month', order_date)
            """;

    public static final String BY_CODE_ANALYTICS_QUERY = """
            SELECT
                code,
                SUM(revenue) AS revenue,
                SUM(orders) AS orders
            FROM public.staged_processed_orders
            WHERE (? IS NULL OR client = ?)
            GROUP BY code
            ORDER BY code
            """;

    public static final String DETAIL_ANALYTICS_QUERY = """
            SELECT
                order_date,
                code,
                orders,
                revenue,
                revenue_per_order
            FROM public.staged_processed_orders
            WHERE (? IS NULL OR client = ?)
            ORDER BY order_date
            """;
}
