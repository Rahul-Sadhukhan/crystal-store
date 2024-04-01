package com.walmart.realestate.crystal.report.util;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class SpreadsheetMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        super.generateHeader(bean);

        final int numColumns = getFieldMap().values().size();
        String[] header = new String[numColumns];
        for (int i = 0; i < numColumns; i++) {
            BeanField<T, Integer> beanField = findField(i);
            String columnHeaderName = extractHeaderName(beanField);
            header[i] = columnHeaderName;
        }
        return header;
    }

    private String extractHeaderName(final BeanField<T, Integer> beanField) {
        return Optional.ofNullable(beanField)
                .map(BeanField::getField)
                .map(field -> field.getDeclaredAnnotationsByType(CsvBindByName.class))
                .filter(array -> array.length != 0)
                .map(array -> array[0])
                .map(CsvBindByName::column)
                .orElse(StringUtils.EMPTY);
    }

}
