package com.example.analytics.repository;

import com.example.analytics.dto.AdscribeAnalyticsResponseDto;
import com.example.analytics.dto.AdscribeDetailDto;
import com.example.analytics.dto.AdscribeKpiResponseDto;
import com.example.analytics.dto.ClientRevenueDto;
import com.example.analytics.dto.DailyPerformanceDto;
import com.example.analytics.dto.ShowRevenueDto;
import com.example.analytics.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class AdscribeAnalyticsRepositoryImpl implements AdscribeAnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdscribeAnalyticsRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AdscribeKpiResponseDto getKpiSummary(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Triggering Adscribe KPI query for {} to {}", startDate, endDate);
            return jdbcTemplate.queryForObject(
                    AdscribeQueryConstants.ADSCRIBE_KPI_QUERY,
                    adscribeKpiRowMapper(),
                    Date.valueOf(startDate),
                    Date.valueOf(endDate)
            );
        } catch (EmptyResultDataAccessException ex) {
            log.warn("No Adscribe KPI rows found for {} to {}", startDate, endDate);
            return emptyKpiResponse();
        } catch (Exception ex) {
            log.error("Error executing Adscribe KPI query", ex);
            throw new DatabaseException("Failed to fetch Adscribe KPI data from Redshift.", ex);
        }
    }

    @Override
    public AdscribeAnalyticsResponseDto getAnalytics(LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Triggering Adscribe analytics queries for {} to {}", startDate, endDate);

            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);

            List<DailyPerformanceDto> daily = jdbcTemplate.query(
                    AdscribeQueryConstants.ADSCRIBE_DAILY_QUERY,
                    dailyPerformanceRowMapper(),
                    start,
                    end
            );

            List<ClientRevenueDto> byClient = jdbcTemplate.query(
                    AdscribeQueryConstants.ADSCRIBE_BY_CLIENT_QUERY,
                    clientRevenueRowMapper(),
                    start,
                    end
            );

            List<ShowRevenueDto> topShows = jdbcTemplate.query(
                    AdscribeQueryConstants.ADSCRIBE_TOP_SHOWS_QUERY,
                    showRevenueRowMapper(),
                    start,
                    end
            );

            List<AdscribeDetailDto> detail = jdbcTemplate.query(
                    AdscribeQueryConstants.ADSCRIBE_DETAIL_QUERY,
                    adscribeDetailRowMapper(),
                    start,
                    end
            );

            return AdscribeAnalyticsResponseDto.builder()
                    .daily(safeList(daily))
                    .byClient(safeList(byClient))
                    .topShows(safeList(topShows))
                    .detail(safeList(detail))
                    .build();
        } catch (Exception ex) {
            log.error("Error executing Adscribe analytics queries", ex);
            throw new DatabaseException("Failed to fetch Adscribe analytics data from Redshift.", ex);
        }
    }

    private RowMapper<AdscribeKpiResponseDto> adscribeKpiRowMapper() {
        return (resultSet, rowNum) -> AdscribeKpiResponseDto.builder()
                .totalRevenue(toBigDecimal(resultSet.getObject("total_revenue")))
                .totalOrders(toLong(resultSet.getObject("total_orders")))
                .totalImpressions(toLong(resultSet.getObject("total_impressions")))
                .avgRevenuePerOrder(toBigDecimal(resultSet.getObject("avg_revenue_per_order")))
                .avgRevenuePerImpression(toBigDecimal(resultSet.getObject("avg_revenue_per_impression")))
                .avgImpressionsPerOrder(toBigDecimal(resultSet.getObject("avg_impressions_per_order")))
                .build();
    }

    private RowMapper<DailyPerformanceDto> dailyPerformanceRowMapper() {
        return (resultSet, rowNum) -> DailyPerformanceDto.builder()
                .date(resultSet.getDate("date") != null 
                        ? resultSet.getDate("date").toLocalDate() 
                        : null)
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .orders(toLong(resultSet.getObject("orders")))
                .impressions(toLong(resultSet.getObject("impressions")))
                .build();
    }

    private RowMapper<ClientRevenueDto> clientRevenueRowMapper() {
        return (resultSet, rowNum) -> ClientRevenueDto.builder()
                .clientName(resultSet.getString("client_name"))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .build();
    }

    private RowMapper<ShowRevenueDto> showRevenueRowMapper() {
        return (resultSet, rowNum) -> ShowRevenueDto.builder()
                .showName(resultSet.getString("show_name"))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .build();
    }

    private RowMapper<AdscribeDetailDto> adscribeDetailRowMapper() {
        return (resultSet, rowNum) -> AdscribeDetailDto.builder()
                .reportDate(resultSet.getDate("report_date") != null 
                        ? resultSet.getDate("report_date").toLocalDate() 
                        : null)
                .clientName(resultSet.getString("client_name"))
                .showName(resultSet.getString("show_name"))
                .revenue(toBigDecimal(resultSet.getObject("revenue")))
                .orders(toLong(resultSet.getObject("orders")))
                .impressions(toLong(resultSet.getObject("impressions")))
                .revenuePerOrder(toBigDecimal(resultSet.getObject("revenue_per_order")))
                .revenuePerImpression(toBigDecimal(resultSet.getObject("revenue_per_impression")))
                .impressionsPerOrder(toBigDecimal(resultSet.getObject("impressions_per_order")))
                .build();
    }

    private AdscribeKpiResponseDto emptyKpiResponse() {
        return AdscribeKpiResponseDto.builder()
                .totalRevenue(BigDecimal.ZERO)
                .totalOrders(0L)
                .totalImpressions(0L)
                .avgRevenuePerOrder(BigDecimal.ZERO)
                .avgRevenuePerImpression(BigDecimal.ZERO)
                .avgImpressionsPerOrder(BigDecimal.ZERO)
                .build();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }

        String str = value.toString();
        if (str.length() >= 10) {
            return LocalDate.parse(str.substring(0, 10));
        }

        throw new IllegalArgumentException("Cannot parse date: " + value);
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