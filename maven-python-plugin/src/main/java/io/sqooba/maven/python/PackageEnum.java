package io.sqooba.maven.python;

/**
 * Enumerate containing most python packaging methods
 */
public enum PackageEnum {
        sdist("sdist"),
        bdist_egg("bdist_egg"),
        bdist_wheel("bdist_wheel");

        private final String text;

        /**
         * @param text
         */
        PackageEnum(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
}
