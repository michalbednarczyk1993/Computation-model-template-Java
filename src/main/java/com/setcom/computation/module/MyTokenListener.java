package com.setcom.computation.module;

import com.setcom.computation.balticlsc.*;
import com.setcom.computation.datamodel.Status;
import ilog.cplex.*; // CPLEX
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MyTokenListener extends TokenListener {

        public MyTokenListener(IJobRegistry registry, IDataHandler data) {
                super(registry, data);
        }

        @Override
        public void dataReceived(String pinName) {
                registry.setStatus(Status.WORKING);
                try {
                        String folderPath = data.obtainDataItem("Folder Name"); // Item / items
                        // In case the data is simple and is wholly contained in the tokens,
                        // you should use operations from the IJobRegistry interface:
                        //  GetPinValue, GetPinValues, GetPinValuesNDim


                        if (Files.isDirectory(Paths.get(folderPath))) {
                                List<Path> filePathList = Files.list(Paths.get(folderPath)).collect(Collectors.toList());
                                int i = 0;
                                for (Path path : filePathList) {
                                        File f = new File(String.valueOf(path));

                                        // mogę przetwarzać jakoś te dane

                                        registry.setProgress(++i/filePathList.size());
                                }
                        } else {
                                File file = new File(String.valueOf(folderPath));

                                // mogę przetwarzać jakoś te dane

                        }
                } catch (Exception e) {
                        log.error(e.toString());
                        registry.setStatus(Status.FAILED);
                        return;
                }
                data.finishProcessing();
        }

        public void optionalDataReceived(String pinName)
        {
            // Place your code here:

        }

        public void dataReady()
        {
            // Place your code here:

        }

        public void dataComplete()
        {
            // Place your code here:

        }
}
