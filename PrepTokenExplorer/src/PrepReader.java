

import java.io.*;

public class PrepReader {
	private int valueOfHexChar(byte b) throws PrepReaderError {
		if ('0' <= b && b <= '9') {
			return b - '0';
		}
		else if ('a' <= b && b <= 'f') {
			return b - 'a' + 0xa;
		}
		else if ('A' <= b && b <= 'F') {
			return b - 'A' + 0xa;
		}
		else {
			throw new PrepReaderError();
		}
	}
	private int valueOfHexString(byte[] buffer, int begin, int end) throws PrepReaderError {
		int value = 0;
		for (int i = begin; i < end; ++i) {
			value <<= 4;
			value += valueOfHexChar(buffer[i]);
		}
		return value;
	}
	public PrepToken[] read(String sourceFilePath, String postfix) throws FileNotFoundException, IOException, PrepReaderError {
		File prepfile = new File(sourceFilePath + "." + postfix);
		System.out.println(prepfile.getAbsolutePath());
		byte[] buffer = getBytesFromFile(prepfile);
		if (buffer == null) {
			throw new IOException();
		}
		
		int newLineCount = 0;
		for (int i = 0; i < buffer.length; ++i) {
			if (buffer[i] == 0x0a) {
				++newLineCount;
			}
		}
		
		PrepToken[] tokens = new PrepToken[newLineCount];
		int i = 0;
		// hex.hex.hex\thex.hex.hex\tstring
		// hex.hex.hex\t+hex\tstring
		int p = 0;
		while (p < buffer.length) {
			int sep0 = p - 1;
			
			while (p < buffer.length && buffer[p] != '.') {
				++p;
			}
			if (! (p < buffer.length)) {
				throw new PrepReaderError();
			}
			int sep1 = p;
			++p;
			
			while (p < buffer.length && buffer[p] != '.') {
				++p;
			}
			if (! (p < buffer.length)) {
				throw new PrepReaderError();
			}
			int sep2 = p;
			++p;
			
			while (p < buffer.length && buffer[p] != '\t') {
				++p;
			}
			if (! (p < buffer.length)) {
				throw new PrepReaderError();
			}
			int sep3 = p;
			++p;
			
			int beginRow = valueOfHexString(buffer, sep0 + 1, sep1);
			int beginCol = valueOfHexString(buffer, sep1 + 1, sep2);
			int beginIndex = valueOfHexString(buffer, sep2 + 1, sep3);
			
			int endRow;
			int endCol;
			int endIndex;
			int sep6;
			if (p < buffer.length && buffer[p] == '+') {
				while (p < buffer.length && buffer[p] != '\t') {
					++p;
				}
				if (! (p < buffer.length)) {
					throw new PrepReaderError();
				}
				sep6 = p;
				++p;

				int w = valueOfHexString(buffer, sep3 + 2, sep6);
				endRow = beginRow;
				endCol = beginCol + w;
				endIndex = beginIndex + w;
			}
			else {
				while (p < buffer.length && buffer[p] != '.') {
					++p;
				}
				if (! (p < buffer.length)) {
					throw new PrepReaderError();
				}
				int sep4 = p;
				++p;
				
				while (p < buffer.length && buffer[p] != '.') {
					++p;
				}
				if (! (p < buffer.length)) {
					throw new PrepReaderError();
				}
				int sep5 = p;
				++p;
				
				while (p < buffer.length && buffer[p] != '\t') {
					++p;
				}
				if (! (p < buffer.length)) {
					throw new PrepReaderError();
				}
				sep6 = p;
				++p;

				endRow = valueOfHexString(buffer, sep3 + 1, sep4);
				endCol = valueOfHexString(buffer, sep4 + 1, sep5);
				endIndex = valueOfHexString(buffer, sep5 + 1, sep6);
			}
			
			while (p < buffer.length && buffer[p] != '\n') {
				++p;
			}
			if (! (p < buffer.length)) {
				throw new PrepReaderError();
			}
			int sep7 = p;
			++p;
			
			int dustLen = 0;
			if (buffer[sep7 - 1] == '\r') {
				dustLen = 1;
			}
				
			String str = new String(buffer, sep6 + 1, sep7 - (sep6 + 1) - dustLen);
			
			tokens[i] = PrepToken.create(beginRow, beginCol, beginIndex, endRow, endCol, endIndex, str);
			++i;
		}
		assert i == tokens.length;
		
		return tokens;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, PrepReaderError {
		{
			File inputFile = new File(args[0]);
			String postfix = args[1];
			String inputFileText = getContents(inputFile);
			int tstart = Integer.parseInt(args[2]);
			int tend = Integer.parseInt(args[3]);
			
			PrepReader reader = new PrepReader();
			PrepToken[] tokens = reader.read(inputFile.getAbsolutePath(), postfix);
			System.out.println(inputFileText.substring(tokens[tstart].beginIndex,tokens[tend].endIndex));
			
		}
		System.exit(0);
	}
	
	
	public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	static public String getContents(File aFile) {
	    //...checks on aFile are elided
	    StringBuilder contents = new StringBuilder();
	    
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    return contents.toString();
	  }
}
