/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl;

import java.util.LinkedList;
import java.util.List;


/**
 * Implementation of the convertor design pattern.
 * 
 * @author fcorneli
 * 
 */
public class ConvertorUtil {

    private ConvertorUtil() {

        // empty
    }

    /**
     * Converts one list to another one.
     * 
     * @param <TypeIn>
     * @param <TypeOut>
     * @param inputList
     * @param convertor
     */
    public static <TypeIn, TypeOut> List<TypeOut> convert(List<TypeIn> inputList, Convertor<TypeIn, TypeOut> convertor) {

        List<TypeOut> outputList = new LinkedList<TypeOut>();
        for (TypeIn inputEntry : inputList) {
            TypeOut outputEntry = convertor.convert(inputEntry);
            outputList.add(outputEntry);
        }
        return outputList;
    }
}
