package erne.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
	public static void serialize(String file, Object object) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(object);
		out.close();
		fileOut.close();
	}

	public static Object deserialize(String file) throws ClassNotFoundException, IOException {
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object object = in.readObject();
		in.close();
		fileIn.close();
		return object;
	}
}
