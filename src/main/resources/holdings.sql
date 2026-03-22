-- 보유종목 관리 테이블
CREATE TABLE IF NOT EXISTS public.holdings (
    id           bigserial    PRIMARY KEY,
    market       text         NOT NULL DEFAULT 'KR'
                              CHECK (market IN ('KR', 'NAS', 'NYS', 'AMS')),
    stock_code   text         NOT NULL,
    symbol       text,
    buy_price    numeric(18,2) NOT NULL,
    buy_date     date         NOT NULL DEFAULT CURRENT_DATE,
    sell_price   numeric(18,2),
    sell_date    date,
    is_sold      boolean      NOT NULL DEFAULT false,
    profit_rate  numeric(10,2) GENERATED ALWAYS AS (
        CASE WHEN sell_price IS NOT NULL AND buy_price > 0
             THEN ROUND((sell_price - buy_price) / buy_price * 100, 2)
             ELSE NULL
        END
    ) STORED,
    notified_5pct  boolean NOT NULL DEFAULT false,
    notified_10pct boolean NOT NULL DEFAULT false,
    created_at   timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_holdings_is_sold ON public.holdings (is_sold);
CREATE INDEX IF NOT EXISTS idx_holdings_market  ON public.holdings (market);

COMMENT ON TABLE  public.holdings IS '보유종목 관리: 매수/매도 기록 + 수익률 자동계산 + 5%/10% 수익 알림';
COMMENT ON COLUMN public.holdings.profit_rate IS '매도가 존재 시 자동 계산 (sell_price - buy_price) / buy_price * 100';
COMMENT ON COLUMN public.holdings.notified_5pct IS '현재가 5% 이상 상승 알림 발송 여부';
COMMENT ON COLUMN public.holdings.notified_10pct IS '현재가 10% 이상 상승 알림 발송 여부';


INSERT INTO public.holdings (market, stock_code, symbol, buy_price, buy_date)
VALUES ('KR', '005930', '삼성전자', 72000, '2026-02-27');

INSERT INTO public.holdings (market, stock_code, symbol, buy_price, buy_date)
VALUES ('KR', '005930', '삼성전자', 72000, '2026-02-27');