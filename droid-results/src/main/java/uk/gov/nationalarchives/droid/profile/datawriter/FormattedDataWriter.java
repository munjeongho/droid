/*
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.profile.datawriter;

import com.univocity.parsers.csv.CsvWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.profile.CsvWriterConstants;
import uk.gov.nationalarchives.droid.profile.ProfileResourceNode;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract class used fow writing headers and lines to the CSV writer.
 * This class implements some common methods and other specific data writers are responsible for overriding
 * the methods for writing headers and data
 */
public abstract  class FormattedDataWriter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String[] customisedHeaders;

    public abstract void writeHeadersForOneRowPerFile(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter);
    public abstract void writeDataRowsForOneRowPerFile(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter);
    public abstract void writeHeadersForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, String[] headers, CsvWriter csvWriter);
    public  abstract void writeDataRowsForOneRowPerFormat(List<? extends ProfileResourceNode> nodes, CsvWriter csvWriter);

    protected static String nullSafeName(Enum<?> value) {
        return value == null ? CsvWriterConstants.EMPTY_STRING : value.toString();
    }

    protected static String nullSafeNumber(Number number) {
        return number == null ? CsvWriterConstants.EMPTY_STRING : number.toString();
    }

    protected static String nullSafeDate(Date date, FastDateFormat format) {
        return date == null ? CsvWriterConstants.EMPTY_STRING : format.format(date);
    }

    protected static String toFileName(String name) {
        return FilenameUtils.getName(name);
    }

    protected String toFilePath(URI uri) {
        if (uri == null) {
            log.warn("[URI not set]");
            return CsvWriterConstants.EMPTY_STRING;
        }
        if (CsvWriterConstants.FILE_URI_SCHEME.equals(uri.getScheme())) {
            return Paths.get(uri).toAbsolutePath().toString();
        }

        // for URIs that have other than "file" scheme
        String result = java.net.URLDecoder.decode(uri.toString()).replaceAll("file://", CsvWriterConstants.EMPTY_STRING);
        result = result.replace("/", File.separator);

        // Handle substitution of 7z
        final String sevenZedIdentifier = "sevenz:";
        if (result.startsWith(sevenZedIdentifier)) {
            result = "7z:" + result.substring(sevenZedIdentifier.length());
        }

        return result;
    }

    protected int getMaxIdentificationCount(List<? extends ProfileResourceNode> nodes) {
        Optional<Integer> maxIdentificationsOption = nodes.stream().map(ProfileResourceNode::getIdentificationCount).collect(Collectors.toList()).stream().filter(Objects::nonNull).max(Integer::compare);
        int maxIdentifications = 0;
        if (maxIdentificationsOption.isPresent()) {
            maxIdentifications = maxIdentificationsOption.get();
        }
        return maxIdentifications;
    }


    protected void setCustomisedHeaders(final String[] customisedHeaders) {
        this.customisedHeaders = Arrays.copyOf(customisedHeaders, customisedHeaders.length);
    }

    protected String[] getCustomisedHeaders() {
        return this.customisedHeaders;
    }
}
