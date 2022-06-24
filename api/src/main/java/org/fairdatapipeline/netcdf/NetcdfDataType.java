package org.fairdatapipeline.netcdf;

import org.jetbrains.annotations.NotNull;
import ucar.ma2.DataType;

import java.lang.reflect.Array;

public enum NetcdfDataType {
        BOOLEAN,
        BYTE,
        CHAR,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        SEQUENCE,
        STRING;


        DataType translate() {
                return NetcdfDataType.translate(this);
        }

        public static DataType translate(NetcdfDataType dataType) {
                switch(dataType) {
                        case BOOLEAN:
                                return DataType.BOOLEAN;
                        case BYTE:
                                return DataType.BYTE;
                        case CHAR:
                                return DataType.CHAR;
                        case SHORT:
                                return DataType.SHORT;
                        case INT:
                                return DataType.INT;
                        case LONG:
                                return DataType.LONG;
                        case FLOAT:
                                return DataType.FLOAT;
                        case DOUBLE:
                                return DataType.DOUBLE;
                        case SEQUENCE:
                                return DataType.SEQUENCE;
                        case STRING:
                                return DataType.STRING;
                }
                return DataType.BOOLEAN;
        }

        public static NetcdfDataType translate_datatype(@NotNull Object o) {
                if (Array.newInstance(Integer.class, 0).getClass().equals(o.getClass()) || Array.newInstance(int.class, 0).getClass().equals(o.getClass())) {
                        return NetcdfDataType.INT;
                } else if (Array.newInstance(Double.class, 0).getClass().equals(o.getClass()) || Array.newInstance(double.class, 0).getClass().equals(o.getClass())) {
                        return NetcdfDataType.DOUBLE;
                } else if(Array.newInstance(String.class, 0).getClass().equals(o.getClass())) {
                        return NetcdfDataType.STRING;
                }
                throw(new UnsupportedOperationException("can't translate object of class "+ o.getClass().getSimpleName() + " to NetCDF data type."));
        }



        public static ucar.ma2.Array translate_array(@NotNull Object o) {
                return ucar.ma2.Array.factory(translate(translate_datatype(o)), new int[] {Array.getLength(o)}, o);
        }

        /**
         *
         * @param dataType
         * @param dim_sizes
         * @param o
         * @return
         */
        public static ucar.ma2.Array translate_array(@NotNull NetcdfDataType dataType, @NotNull int[] dim_sizes, @NotNull Object o) {
                return ucar.ma2.Array.factory(dataType.translate(), dim_sizes, o);
        }
}
