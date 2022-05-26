package org.fairdatapipeline.netcdf;

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
                switch(this) {
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

        public static DataType translate_datatype(Object o) {
                if (Array.newInstance(Integer.class, 0).getClass().equals(o.getClass()) || Array.newInstance(int.class, 0).getClass().equals(o.getClass())) {
                        return DataType.INT;
                } else if (Array.newInstance(Double.class, 0).getClass().equals(o.getClass()) || Array.newInstance(double.class, 0).getClass().equals(o.getClass())) {
                        return DataType.DOUBLE;
                } else if(Array.newInstance(String.class, 0).getClass().equals(o.getClass())) {
                        return DataType.STRING;
                }
                throw(new UnsupportedOperationException("can't translate object of class "+ o.getClass().getSimpleName() + " to NetCDF data type."));
        }

        /**
         *
         * @param o an array cast to Object; can be an array of Object or int or double.
         * @return the length of the array
         */
        static int get_array_length(Object o) {
                if(o.getClass().equals(int[].class)) return ((int[])o).length;
                if(o.getClass().equals(double[].class)) return ((double[])o).length;
                try {
                        return ((Object[])o).length;
                }catch(ClassCastException e) {
                        throw(new IllegalArgumentException("can't find the length of " + o.getClass().getSimpleName(), e));
                }

        }

        public static ucar.ma2.Array translate_array(Object o) {
                DataType dt = translate_datatype(o);
                System.out.println("data type: " + dt);
                return ucar.ma2.Array.factory(dt, new int[] {get_array_length(o)}, o);
        }

        /**
         *
         * @param dataType
         * @param dim_sizes
         * @param o
         * @return
         */
        public static ucar.ma2.Array translate_array(NetcdfDataType dataType, int[] dim_sizes, Object o) {
                return ucar.ma2.Array.factory(dataType.translate(), dim_sizes, o);
        }
}
