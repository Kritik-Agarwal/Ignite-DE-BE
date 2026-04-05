package com.example.analytics.repository;

import com.example.analytics.dto.AnalyticsResponseDto;
import com.example.analytics.dto.CodeStatsDto;
import com.example.analytics.dto.DailyDto;
import com.example.analytics.dto.DetailDto;
import com.example.analytics.dto.KpiResponseDto;
import com.example.analytics.dto.MonthlyDto;
import com.example.analytics.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public KpiResponseDto fetchKpiSummary(String client) {
        try {
            log.info("Executing KPI query for client filter={}", client == null ? "ALL" : client);

            return jdbcTemplate.queryForObject(
                    QueryConstants.KPI_QUERY,
                    kpiRowMapper(),
                    client,
                    client
            );
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No KPI data found for client filter={}", client == null ? "ALL" : client);
            return emptyKpiResponse();
        } catch (Exception ex) {
            log.error("Error executing KPI query", ex);
            throw new DatabaseException("Failed to fetch KPI data from Redshift.", ex);
        }
    }

    @Override
    public AnalyticsResponseDto fetchAnalytics(String client) {
        try {
            log.info("Executing analytics queries for client filter={}", client == null ? "ALL" : client);

            List<DailyDto> daily = jdbcTemplate.query(
                    QueryConstants.DAILY_ANALYTICS_QUERY,
                    dailyRowMapper(),
                    client,
                    client
            );

            List<MonthlyDto> monthly = jdbcTemplate.query(
                    QueryConstants.MONTHLY_ANALYTICS_QUERY,
                    monthlyRowMapper(),
                    client,
                    client
            );

            List<CodeStatsDto> byCode = jdbcTemplate.query(
                    QueryConstants.BY_CODE_ANALYTICS_QUERY,
                    codeStatsRowMapper(),
                    client,
                    client
            );

            List<DetailDto> detail = jdbcTemplate.query(
                    QueryConstants.DETAIL_ANALYTICS_QUERY,
                    detailRowMapper(),
                    client,
                    client
            );

            return AnalyticsResponseDto.builder()
                    .daily(safeList(daily))
                    .monthly(safeList(monthly))
                    .byCode(safeList(byCode))
                    .detail(safeList(detail))
                    .build();
        } catch (Exception ex) {
            log.error("Error executing analytics queries", ex);
            throw new DatabaseException("Failed to execute analytics queries.", ex);
        }
    }

    private RowMapper<KpiResponseDto> kpiRowMapper() {
        return (resultSet, rowNum) -> KpiResponseDto.builder()
                .totalOrders(toLong(resultSet.getObject("total_orders")))
                .totalRevenue(toBigDecimal(resultSet.getObject("total_revenue")))
                .avgRevenuePerOrder(toBigDecimal(resultSet.getObject("avg_revenue_per_order")))
                .build();
    }

    private RowMapper<DailyDto> dailyRowMapper() {
        return (resultSet, rowNum) -> DailyDto.builder()
                .date(toLocalDate(resultSet.getObject("date")))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .orders(toLong(resultSet.getObject("orders")))
                .build();
    }

    private RowMapper<MonthlyDto> monthlyRowMapper() {
        return (resultSet, rowNum) -> MonthlyDto.builder()
                .month(toLocalDateTime(resultSet.getObject("month")))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .build();
    }

    private RowMapper<CodeStatsDto> codeStatsRowMapper() {
        return (resultSet, rowNum) -> CodeStatsDto.builder()
                .code(resultSet.getString("code"))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .orders(toLong(resultSet.getObject("orders")))
                .build();
    }

    private RowMapper<DetailDto> detailRowMapper() {
        return (resultSet, rowNum) -> DetailDto.builder()
                .orderDate(toLocalDateTime(resultSet.getObject("order_date")))
                .code(resultSet.getString("code"))
                .orders(toLong(resultSet.getObject("orders")))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .revenuePerOrder(toBigDecimal(resultSet.getObject("revenue_per_order")))
                .build();
    }

    private KpiResponseDto emptyKpiResponse() {
        return KpiResponseDto.builder()
                .totalOrders(0L)
                .totalRevenue(BigDecimal.ZERO)
                .avgRevenuePerOrder(BigDecimal.ZERO)
                .build();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return Timestamp.valueOf(value.toString()).toLocalDateTime();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
