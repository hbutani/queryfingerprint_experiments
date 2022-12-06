package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;
import gudusoft.gsqlparser.sqlenv.TSQLSchema;
import gudusoft.gsqlparser.sqlenv.TSQLTable;

public class TPCDSSQLEnv extends TSQLEnv {

    public TPCDSSQLEnv(EDbVendor dbVendor) {
        super(dbVendor);
        initSQLEnv();
    }

    public String getDefaultSchemaName() {
        return "tpcds";
    }

    @Override
    public void initSQLEnv() {

        TSQLSchema sqlSchema = createSQLSchema("DEFAULT.tpcds");

// Table CALL_CENTER
        TSQLTable call_center = sqlSchema.createTable("call_center");
        call_center.addColumn("cc_call_center_sk");
        call_center.addColumn("cc_call_center_id");
        call_center.addColumn("cc_rec_start_date");
        call_center.addColumn("cc_rec_end_date");
        call_center.addColumn("cc_closed_date_sk");
        call_center.addColumn("cc_open_date_sk");
        call_center.addColumn("cc_name");
        call_center.addColumn("cc_class");
        call_center.addColumn("cc_employees");
        call_center.addColumn("cc_sq_ft");
        call_center.addColumn("cc_hours");
        call_center.addColumn("cc_manager");
        call_center.addColumn("cc_mkt_id");
        call_center.addColumn("cc_mkt_class");
        call_center.addColumn("cc_mkt_desc");
        call_center.addColumn("cc_market_manager");
        call_center.addColumn("cc_division");
        call_center.addColumn("cc_division_name");
        call_center.addColumn("cc_company");
        call_center.addColumn("cc_company_name");
        call_center.addColumn("cc_street_number");
        call_center.addColumn("cc_street_name");
        call_center.addColumn("cc_street_type");
        call_center.addColumn("cc_suite_number");
        call_center.addColumn("cc_city");
        call_center.addColumn("cc_county");
        call_center.addColumn("cc_state");
        call_center.addColumn("cc_zip");
        call_center.addColumn("cc_country");
        call_center.addColumn("cc_gmt_offset");
        call_center.addColumn("cc_tax_percentage");
// End Table CALL_CENTER
// Table CATALOG_PAGE
        TSQLTable catalog_page = sqlSchema.createTable("catalog_page");
        catalog_page.addColumn("cp_catalog_page_sk");
        catalog_page.addColumn("cp_catalog_page_id");
        catalog_page.addColumn("cp_start_date_sk");
        catalog_page.addColumn("cp_end_date_sk");
        catalog_page.addColumn("cp_department");
        catalog_page.addColumn("cp_catalog_number");
        catalog_page.addColumn("cp_catalog_page_number");
        catalog_page.addColumn("cp_description");
        catalog_page.addColumn("cp_type");
// End Table CATALOG_PAGE
// Table CATALOG_RETURNS
        TSQLTable catalog_returns = sqlSchema.createTable("catalog_returns");
        catalog_returns.addColumn("cr_returned_date_sk");
        catalog_returns.addColumn("cr_returned_time_sk");
        catalog_returns.addColumn("cr_item_sk");
        catalog_returns.addColumn("cr_refunded_customer_sk");
        catalog_returns.addColumn("cr_refunded_cdemo_sk");
        catalog_returns.addColumn("cr_refunded_hdemo_sk");
        catalog_returns.addColumn("cr_refunded_addr_sk");
        catalog_returns.addColumn("cr_returning_customer_sk");
        catalog_returns.addColumn("cr_returning_cdemo_sk");
        catalog_returns.addColumn("cr_returning_hdemo_sk");
        catalog_returns.addColumn("cr_returning_addr_sk");
        catalog_returns.addColumn("cr_call_center_sk");
        catalog_returns.addColumn("cr_catalog_page_sk");
        catalog_returns.addColumn("cr_ship_mode_sk");
        catalog_returns.addColumn("cr_warehouse_sk");
        catalog_returns.addColumn("cr_reason_sk");
        catalog_returns.addColumn("cr_order_number");
        catalog_returns.addColumn("cr_return_quantity");
        catalog_returns.addColumn("cr_return_amount");
        catalog_returns.addColumn("cr_return_tax");
        catalog_returns.addColumn("cr_return_amt_inc_tax");
        catalog_returns.addColumn("cr_fee");
        catalog_returns.addColumn("cr_return_ship_cost");
        catalog_returns.addColumn("cr_refunded_cash");
        catalog_returns.addColumn("cr_reversed_charge");
        catalog_returns.addColumn("cr_store_credit");
        catalog_returns.addColumn("cr_net_loss");
// End Table CATALOG_RETURNS
// Table CATALOG_SALES
        TSQLTable catalog_sales = sqlSchema.createTable("catalog_sales");
        catalog_sales.addColumn("cs_sold_date_sk");
        catalog_sales.addColumn("cs_sold_time_sk");
        catalog_sales.addColumn("cs_ship_date_sk");
        catalog_sales.addColumn("cs_bill_customer_sk");
        catalog_sales.addColumn("cs_bill_cdemo_sk");
        catalog_sales.addColumn("cs_bill_hdemo_sk");
        catalog_sales.addColumn("cs_bill_addr_sk");
        catalog_sales.addColumn("cs_ship_customer_sk");
        catalog_sales.addColumn("cs_ship_cdemo_sk");
        catalog_sales.addColumn("cs_ship_hdemo_sk");
        catalog_sales.addColumn("cs_ship_addr_sk");
        catalog_sales.addColumn("cs_call_center_sk");
        catalog_sales.addColumn("cs_catalog_page_sk");
        catalog_sales.addColumn("cs_ship_mode_sk");
        catalog_sales.addColumn("cs_warehouse_sk");
        catalog_sales.addColumn("cs_item_sk");
        catalog_sales.addColumn("cs_promo_sk");
        catalog_sales.addColumn("cs_order_number");
        catalog_sales.addColumn("cs_quantity");
        catalog_sales.addColumn("cs_wholesale_cost");
        catalog_sales.addColumn("cs_list_price");
        catalog_sales.addColumn("cs_sales_price");
        catalog_sales.addColumn("cs_ext_discount_amt");
        catalog_sales.addColumn("cs_ext_sales_price");
        catalog_sales.addColumn("cs_ext_wholesale_cost");
        catalog_sales.addColumn("cs_ext_list_price");
        catalog_sales.addColumn("cs_ext_tax");
        catalog_sales.addColumn("cs_coupon_amt");
        catalog_sales.addColumn("cs_ext_ship_cost");
        catalog_sales.addColumn("cs_net_paid");
        catalog_sales.addColumn("cs_net_paid_inc_tax");
        catalog_sales.addColumn("cs_net_paid_inc_ship");
        catalog_sales.addColumn("cs_net_paid_inc_ship_tax");
        catalog_sales.addColumn("cs_net_profit");
// End Table CATALOG_SALES
// Table CUSTOMER
        TSQLTable customer = sqlSchema.createTable("customer");
        customer.addColumn("c_customer_sk");
        customer.addColumn("c_customer_id");
        customer.addColumn("c_current_cdemo_sk");
        customer.addColumn("c_current_hdemo_sk");
        customer.addColumn("c_current_addr_sk");
        customer.addColumn("c_first_shipto_date_sk");
        customer.addColumn("c_first_sales_date_sk");
        customer.addColumn("c_salutation");
        customer.addColumn("c_first_name");
        customer.addColumn("c_last_name");
        customer.addColumn("c_preferred_cust_flag");
        customer.addColumn("c_birth_day");
        customer.addColumn("c_birth_month");
        customer.addColumn("c_birth_year");
        customer.addColumn("c_birth_country");
        customer.addColumn("c_login");
        customer.addColumn("c_email_address");
        customer.addColumn("c_last_review_date_sk");
// End Table CUSTOMER
// Table CUSTOMER_ADDRESS
        TSQLTable customer_address = sqlSchema.createTable("customer_address");
        customer_address.addColumn("ca_address_sk");
        customer_address.addColumn("ca_address_id");
        customer_address.addColumn("ca_street_number");
        customer_address.addColumn("ca_street_name");
        customer_address.addColumn("ca_street_type");
        customer_address.addColumn("ca_suite_number");
        customer_address.addColumn("ca_city");
        customer_address.addColumn("ca_county");
        customer_address.addColumn("ca_state");
        customer_address.addColumn("ca_zip");
        customer_address.addColumn("ca_country");
        customer_address.addColumn("ca_gmt_offset");
        customer_address.addColumn("ca_location_type");
// End Table CUSTOMER_ADDRESS
// Table CUSTOMER_DEMOGRAPHICS
        TSQLTable customer_demographics = sqlSchema.createTable("customer_demographics");
        customer_demographics.addColumn("cd_demo_sk");
        customer_demographics.addColumn("cd_gender");
        customer_demographics.addColumn("cd_marital_status");
        customer_demographics.addColumn("cd_education_status");
        customer_demographics.addColumn("cd_purchase_estimate");
        customer_demographics.addColumn("cd_credit_rating");
        customer_demographics.addColumn("cd_dep_count");
        customer_demographics.addColumn("cd_dep_employed_count");
        customer_demographics.addColumn("cd_dep_college_count");
// End Table CUSTOMER_DEMOGRAPHICS
// Table DATE_DIM
        TSQLTable date_dim = sqlSchema.createTable("date_dim");
        date_dim.addColumn("d_date_sk");
        date_dim.addColumn("d_date_id");
        date_dim.addColumn("d_date");
        date_dim.addColumn("d_month_seq");
        date_dim.addColumn("d_week_seq");
        date_dim.addColumn("d_quarter_seq");
        date_dim.addColumn("d_year");
        date_dim.addColumn("d_dow");
        date_dim.addColumn("d_moy");
        date_dim.addColumn("d_dom");
        date_dim.addColumn("d_qoy");
        date_dim.addColumn("d_fy_year");
        date_dim.addColumn("d_fy_quarter_seq");
        date_dim.addColumn("d_fy_week_seq");
        date_dim.addColumn("d_day_name");
        date_dim.addColumn("d_quarter_name");
        date_dim.addColumn("d_holiday");
        date_dim.addColumn("d_weekend");
        date_dim.addColumn("d_following_holiday");
        date_dim.addColumn("d_first_dom");
        date_dim.addColumn("d_last_dom");
        date_dim.addColumn("d_same_day_ly");
        date_dim.addColumn("d_same_day_lq");
        date_dim.addColumn("d_current_day");
        date_dim.addColumn("d_current_week");
        date_dim.addColumn("d_current_month");
        date_dim.addColumn("d_current_quarter");
        date_dim.addColumn("d_current_year");
// End Table DATE_DIM
// Table HOUSEHOLD_DEMOGRAPHICS
        TSQLTable household_demographics = sqlSchema.createTable("household_demographics");
        household_demographics.addColumn("hd_demo_sk");
        household_demographics.addColumn("hd_income_band_sk");
        household_demographics.addColumn("hd_buy_potential");
        household_demographics.addColumn("hd_dep_count");
        household_demographics.addColumn("hd_vehicle_count");
// End Table HOUSEHOLD_DEMOGRAPHICS
// Table INCOME_BAND
        TSQLTable income_band = sqlSchema.createTable("income_band");
        income_band.addColumn("ib_income_band_sk");
        income_band.addColumn("ib_lower_bound");
        income_band.addColumn("ib_upper_bound");
// End Table INCOME_BAND
// Table INVENTORY
        TSQLTable inventory = sqlSchema.createTable("inventory");
        inventory.addColumn("inv_date_sk");
        inventory.addColumn("inv_item_sk");
        inventory.addColumn("inv_warehouse_sk");
        inventory.addColumn("inv_quantity_on_hand");
// End Table INVENTORY
// Table ITEM
        TSQLTable item = sqlSchema.createTable("item");
        item.addColumn("i_item_sk");
        item.addColumn("i_item_id");
        item.addColumn("i_rec_start_date");
        item.addColumn("i_rec_end_date");
        item.addColumn("i_item_desc");
        item.addColumn("i_current_price");
        item.addColumn("i_wholesale_cost");
        item.addColumn("i_brand_id");
        item.addColumn("i_brand");
        item.addColumn("i_class_id");
        item.addColumn("i_class");
        item.addColumn("i_category_id");
        item.addColumn("i_category");
        item.addColumn("i_manufact_id");
        item.addColumn("i_manufact");
        item.addColumn("i_size");
        item.addColumn("i_formulation");
        item.addColumn("i_color");
        item.addColumn("i_units");
        item.addColumn("i_container");
        item.addColumn("i_manager_id");
        item.addColumn("i_product_name");
// End Table ITEM
// Table PROMOTION
        TSQLTable promotion = sqlSchema.createTable("promotion");
        promotion.addColumn("p_promo_sk");
        promotion.addColumn("p_promo_id");
        promotion.addColumn("p_start_date_sk");
        promotion.addColumn("p_end_date_sk");
        promotion.addColumn("p_item_sk");
        promotion.addColumn("p_cost");
        promotion.addColumn("p_response_targe");
        promotion.addColumn("p_promo_name");
        promotion.addColumn("p_channel_dmail");
        promotion.addColumn("p_channel_email");
        promotion.addColumn("p_channel_catalog");
        promotion.addColumn("p_channel_tv");
        promotion.addColumn("p_channel_radio");
        promotion.addColumn("p_channel_press");
        promotion.addColumn("p_channel_event");
        promotion.addColumn("p_channel_demo");
        promotion.addColumn("p_channel_details");
        promotion.addColumn("p_purpose");
        promotion.addColumn("p_discount_active");
// End Table PROMOTION
// Table REASON
        TSQLTable reason = sqlSchema.createTable("reason");
        reason.addColumn("r_reason_sk");
        reason.addColumn("r_reason_id");
        reason.addColumn("r_reason_desc");
// End Table REASON
// Table SHIP_MODE
        TSQLTable ship_mode = sqlSchema.createTable("ship_mode");
        ship_mode.addColumn("sm_ship_mode_sk");
        ship_mode.addColumn("sm_ship_mode_id");
        ship_mode.addColumn("sm_type");
        ship_mode.addColumn("sm_code");
        ship_mode.addColumn("sm_carrier");
        ship_mode.addColumn("sm_contract");
// End Table SHIP_MODE
// Table STORE
        TSQLTable store = sqlSchema.createTable("store");
        store.addColumn("s_store_sk");
        store.addColumn("s_store_id");
        store.addColumn("s_rec_start_date");
        store.addColumn("s_rec_end_date");
        store.addColumn("s_closed_date_sk");
        store.addColumn("s_store_name");
        store.addColumn("s_number_employees");
        store.addColumn("s_floor_space");
        store.addColumn("s_hours");
        store.addColumn("s_manager");
        store.addColumn("s_market_id");
        store.addColumn("s_geography_class");
        store.addColumn("s_market_desc");
        store.addColumn("s_market_manager");
        store.addColumn("s_division_id");
        store.addColumn("s_division_name");
        store.addColumn("s_company_id");
        store.addColumn("s_company_name");
        store.addColumn("s_street_number");
        store.addColumn("s_street_name");
        store.addColumn("s_street_type");
        store.addColumn("s_suite_number");
        store.addColumn("s_city");
        store.addColumn("s_county");
        store.addColumn("s_state");
        store.addColumn("s_zip");
        store.addColumn("s_country");
        store.addColumn("s_gmt_offset");
        store.addColumn("s_tax_precentage");
// End Table STORE
// Table STORE_RETURNS
        TSQLTable store_returns = sqlSchema.createTable("store_returns");
        store_returns.addColumn("sr_returned_date_sk");
        store_returns.addColumn("sr_return_time_sk");
        store_returns.addColumn("sr_item_sk");
        store_returns.addColumn("sr_customer_sk");
        store_returns.addColumn("sr_cdemo_sk");
        store_returns.addColumn("sr_hdemo_sk");
        store_returns.addColumn("sr_addr_sk");
        store_returns.addColumn("sr_store_sk");
        store_returns.addColumn("sr_reason_sk");
        store_returns.addColumn("sr_ticket_number");
        store_returns.addColumn("sr_return_quantity");
        store_returns.addColumn("sr_return_amt");
        store_returns.addColumn("sr_return_tax");
        store_returns.addColumn("sr_return_amt_inc_tax");
        store_returns.addColumn("sr_fee");
        store_returns.addColumn("sr_return_ship_cost");
        store_returns.addColumn("sr_refunded_cash");
        store_returns.addColumn("sr_reversed_charge");
        store_returns.addColumn("sr_store_credit");
        store_returns.addColumn("sr_net_loss");
// End Table STORE_RETURNS
// Table STORE_SALES
        TSQLTable store_sales = sqlSchema.createTable("store_sales");
        store_sales.addColumn("ss_sold_date_sk");
        store_sales.addColumn("ss_sold_time_sk");
        store_sales.addColumn("ss_item_sk");
        store_sales.addColumn("ss_customer_sk");
        store_sales.addColumn("ss_cdemo_sk");
        store_sales.addColumn("ss_hdemo_sk");
        store_sales.addColumn("ss_addr_sk");
        store_sales.addColumn("ss_store_sk");
        store_sales.addColumn("ss_promo_sk");
        store_sales.addColumn("ss_ticket_number");
        store_sales.addColumn("ss_quantity");
        store_sales.addColumn("ss_wholesale_cost");
        store_sales.addColumn("ss_list_price");
        store_sales.addColumn("ss_sales_price");
        store_sales.addColumn("ss_ext_discount_amt");
        store_sales.addColumn("ss_ext_sales_price");
        store_sales.addColumn("ss_ext_wholesale_cost");
        store_sales.addColumn("ss_ext_list_price");
        store_sales.addColumn("ss_ext_tax");
        store_sales.addColumn("ss_coupon_amt");
        store_sales.addColumn("ss_net_paid");
        store_sales.addColumn("ss_net_paid_inc_tax");
        store_sales.addColumn("ss_net_profit");
// End Table STORE_SALES
// Table TIME_DIM
        TSQLTable time_dim = sqlSchema.createTable("time_dim");
        time_dim.addColumn("t_time_sk");
        time_dim.addColumn("t_time_id");
        time_dim.addColumn("t_time");
        time_dim.addColumn("t_hour");
        time_dim.addColumn("t_minute");
        time_dim.addColumn("t_second");
        time_dim.addColumn("t_am_pm");
        time_dim.addColumn("t_shift");
        time_dim.addColumn("t_sub_shift");
        time_dim.addColumn("t_meal_time");
// End Table TIME_DIM
// Table WAREHOUSE
        TSQLTable warehouse = sqlSchema.createTable("warehouse");
        warehouse.addColumn("w_warehouse_sk");
        warehouse.addColumn("w_warehouse_id");
        warehouse.addColumn("w_warehouse_name");
        warehouse.addColumn("w_warehouse_sq_ft");
        warehouse.addColumn("w_street_number");
        warehouse.addColumn("w_street_name");
        warehouse.addColumn("w_street_type");
        warehouse.addColumn("w_suite_number");
        warehouse.addColumn("w_city");
        warehouse.addColumn("w_county");
        warehouse.addColumn("w_state");
        warehouse.addColumn("w_zip");
        warehouse.addColumn("w_country");
        warehouse.addColumn("w_gmt_offset");
// End Table WAREHOUSE
// Table WEB_PAGE
        TSQLTable web_page = sqlSchema.createTable("web_page");
        web_page.addColumn("wp_web_page_sk");
        web_page.addColumn("wp_web_page_id");
        web_page.addColumn("wp_rec_start_date");
        web_page.addColumn("wp_rec_end_date");
        web_page.addColumn("wp_creation_date_sk");
        web_page.addColumn("wp_access_date_sk");
        web_page.addColumn("wp_autogen_flag");
        web_page.addColumn("wp_customer_sk");
        web_page.addColumn("wp_url");
        web_page.addColumn("wp_type");
        web_page.addColumn("wp_char_count");
        web_page.addColumn("wp_link_count");
        web_page.addColumn("wp_image_count");
        web_page.addColumn("wp_max_ad_count");
// End Table WEB_PAGE
// Table WEB_RETURNS
        TSQLTable web_returns = sqlSchema.createTable("web_returns");
        web_returns.addColumn("wr_returned_date_sk");
        web_returns.addColumn("wr_returned_time_sk");
        web_returns.addColumn("wr_item_sk");
        web_returns.addColumn("wr_refunded_customer_sk");
        web_returns.addColumn("wr_refunded_cdemo_sk");
        web_returns.addColumn("wr_refunded_hdemo_sk");
        web_returns.addColumn("wr_refunded_addr_sk");
        web_returns.addColumn("wr_returning_customer_sk");
        web_returns.addColumn("wr_returning_cdemo_sk");
        web_returns.addColumn("wr_returning_hdemo_sk");
        web_returns.addColumn("wr_returning_addr_sk");
        web_returns.addColumn("wr_web_page_sk");
        web_returns.addColumn("wr_reason_sk");
        web_returns.addColumn("wr_order_number");
        web_returns.addColumn("wr_return_quantity");
        web_returns.addColumn("wr_return_amt");
        web_returns.addColumn("wr_return_tax");
        web_returns.addColumn("wr_return_amt_inc_tax");
        web_returns.addColumn("wr_fee");
        web_returns.addColumn("wr_return_ship_cost");
        web_returns.addColumn("wr_refunded_cash");
        web_returns.addColumn("wr_reversed_charge");
        web_returns.addColumn("wr_account_credit");
        web_returns.addColumn("wr_net_loss");
// End Table WEB_RETURNS
// Table WEB_SALES
        TSQLTable web_sales = sqlSchema.createTable("web_sales");
        web_sales.addColumn("ws_sold_date_sk");
        web_sales.addColumn("ws_sold_time_sk");
        web_sales.addColumn("ws_ship_date_sk");
        web_sales.addColumn("ws_item_sk");
        web_sales.addColumn("ws_bill_customer_sk");
        web_sales.addColumn("ws_bill_cdemo_sk");
        web_sales.addColumn("ws_bill_hdemo_sk");
        web_sales.addColumn("ws_bill_addr_sk");
        web_sales.addColumn("ws_ship_customer_sk");
        web_sales.addColumn("ws_ship_cdemo_sk");
        web_sales.addColumn("ws_ship_hdemo_sk");
        web_sales.addColumn("ws_ship_addr_sk");
        web_sales.addColumn("ws_web_page_sk");
        web_sales.addColumn("ws_web_site_sk");
        web_sales.addColumn("ws_ship_mode_sk");
        web_sales.addColumn("ws_warehouse_sk");
        web_sales.addColumn("ws_promo_sk");
        web_sales.addColumn("ws_order_number");
        web_sales.addColumn("ws_quantity");
        web_sales.addColumn("ws_wholesale_cost");
        web_sales.addColumn("ws_list_price");
        web_sales.addColumn("ws_sales_price");
        web_sales.addColumn("ws_ext_discount_amt");
        web_sales.addColumn("ws_ext_sales_price");
        web_sales.addColumn("ws_ext_wholesale_cost");
        web_sales.addColumn("ws_ext_list_price");
        web_sales.addColumn("ws_ext_tax");
        web_sales.addColumn("ws_coupon_amt");
        web_sales.addColumn("ws_ext_ship_cost");
        web_sales.addColumn("ws_net_paid");
        web_sales.addColumn("ws_net_paid_inc_tax");
        web_sales.addColumn("ws_net_paid_inc_ship");
        web_sales.addColumn("ws_net_paid_inc_ship_tax");
        web_sales.addColumn("ws_net_profit");
// End Table WEB_SALES
// Table WEB_SITE
        TSQLTable web_site = sqlSchema.createTable("web_site");
        web_site.addColumn("web_site_sk");
        web_site.addColumn("web_site_id");
        web_site.addColumn("web_rec_start_date");
        web_site.addColumn("web_rec_end_date");
        web_site.addColumn("web_name");
        web_site.addColumn("web_open_date_sk");
        web_site.addColumn("web_close_date_sk");
        web_site.addColumn("web_class");
        web_site.addColumn("web_manager");
        web_site.addColumn("web_mkt_id");
        web_site.addColumn("web_mkt_class");
        web_site.addColumn("web_mkt_desc");
        web_site.addColumn("web_market_manager");
        web_site.addColumn("web_company_id");
        web_site.addColumn("web_company_name");
        web_site.addColumn("web_street_number");
        web_site.addColumn("web_street_name");
        web_site.addColumn("web_street_type");
        web_site.addColumn("web_suite_number");
        web_site.addColumn("web_city");
        web_site.addColumn("web_county");
        web_site.addColumn("web_state");
        web_site.addColumn("web_zip");
        web_site.addColumn("web_country");
        web_site.addColumn("web_gmt_offset");
        web_site.addColumn("web_tax_percentage");
// End Table WEB_SITE

    }
}
