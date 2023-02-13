(
    select d_date
    from date_dim
    where d_week_seq = (
        select d_week_seq
        from date_dim
        where d_date = '1998-02-19'
    )
)