package io.diligencevault.plugin.core.tasks;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class ResponceDecoder {
    public String streamToString(InputStream stream)
    {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder stringBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) 
            {
                stringBody.append(line).append("\n");
            }
            return stringBody.toString();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String GZIPToString(InputStream stream) 
    {
        try
        {
            InputStream responseStream = new GZIPInputStream(stream);             
            return streamToString(responseStream);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        
    }
    
}
