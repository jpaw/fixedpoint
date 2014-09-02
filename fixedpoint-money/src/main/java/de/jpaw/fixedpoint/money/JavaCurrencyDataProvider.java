package de.jpaw.fixedpoint.money;

import java.util.Currency;

public class JavaCurrencyDataProvider implements CurrencyDataProvider {
    static public final JavaCurrencyDataProvider instance = new JavaCurrencyDataProvider();
    private JavaCurrencyDataProvider() {
    }
    
    public static class JavaCurrencyData implements CurrencyData {
        private final Currency currency;
        private JavaCurrencyData(Currency currency) {
            this.currency = currency;
        }

        @Override
        public String getCurrencyCode() {
            return currency.getCurrencyCode();
        }

        @Override
        public int getNumericCode() {
            return currency.getNumericCode();
        }

        @Override
        public String getSymbol() {
            return currency.getSymbol();
        }

        @Override
        public String getDisplayName() {
            return currency.getDisplayName();
        }

        @Override
        public int getDefaultFractionDigits() {
            return currency.getDefaultFractionDigits();
        }
        
    }
    @Override
    public CurrencyData get(String key) {
        try {
            return new JavaCurrencyData(Currency.getInstance(key)); 
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void set(String key, CurrencyData data) {
        throw new UnsupportedOperationException("Cannot create new currencies");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot clear the currency list");
    }

    @Override
    public void init() {
    }
}
