import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class GestorHTTP implements HttpHandler {

    //http://localhost:5001/servidor/mostrarUno?name=xxx
    //http://localhost:5000/servidor/mostrarTodos
    //http://localhost:5002/servidor/nuevo
	
	//EJCUTAR CADA SCRIPT SERVIDOR(Uno,Todos,Nuevo)

    String nombreCompleto;
    String alias;
    String fechaNacimiento;
    String nacionalidad;
    String imagen;

    public void log(String clientIP, String formattedDate, String tipo) {

        try (FileWriter fw = new FileWriter("log.txt", true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.println("TIPO: " + tipo);
            out.println("Direccion IP: " + clientIP);
            out.println("Fecha y Hora: " + formattedDate);
            out.println("---------------------------");
        } catch (IOException e) {}
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        if (httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {

            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        System.out.print("Peticion recibida: Tipo ");
        String requestParamValueUno = null;
        String requestParamValueTodos = null;
        String requestParamValueNuevo = null;

        //LOG DE CONEXIONES
        HttpExchange exchange = httpExchange;

        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        long requestTimestamp = System.currentTimeMillis();  //long requestTime = exchange.getRequestTime();  No me ha funcionado getRequestTime y he optado por recoger la hora del sistema
        Date date = new Date(requestTimestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateFormat.format(date);
        System.out.println("Client IP: " + clientIP + " Client time: " + formattedDate);
        

        if ("GET".equalsIgnoreCase(httpExchange.getRequestMethod())) {    //Si recibe peticion get
            System.out.println("GET");													

            if ("/servidor/mostrarUno".equalsIgnoreCase(httpExchange.getRequestURI().getPath())) {   //Si la peticion get es igual a la url /servidor/mostrarUno
                requestParamValueUno = handleGetRequestUno(httpExchange);
                handleGetResponseUno(httpExchange, requestParamValueUno);
                log(clientIP, formattedDate, "BUSQUEDA ESPECIFICA");								//Registra el log
            }

            if ("/servidor/mostrarTodos".equalsIgnoreCase(httpExchange.getRequestURI().getPath())) {     
                requestParamValueTodos = handleGetRequestTodos(httpExchange);
                handleGetResponseTodos(httpExchange);
                log(clientIP, formattedDate, "BUSQUEDA GENERAL");
            }

            if ("/servidor/nuevo".equalsIgnoreCase(httpExchange.getRequestURI().getPath())) {     
                requestParamValueNuevo = handleGetRequestNuevo(httpExchange);
                handleGetResponseNuevo(httpExchange);
                System.out.println("Nuevo");
                log(clientIP, formattedDate, "ISERCION DELINCUENTE");
            }

        } else if ("POST".equalsIgnoreCase(httpExchange.getRequestMethod())) {  //Si recibe peticion post
            System.out.println("POST");
            requestParamValueUno = handlePostRequest(httpExchange);
            handlePostResponse(httpExchange, requestParamValueUno); 
        } else {
            System.out.println("DESCONOCIDA");
        }
    }

    // INICIO BLOQUE REQUEST

    private String handleGetRequestUno(HttpExchange httpExchange) {
        System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
        return httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
    }
    private String handleGetRequestTodos(HttpExchange httpExchange) {
        System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
        return httpExchange.getRequestURI().toString();
    }
    private String handleGetRequestNuevo(HttpExchange httpExchange) {
        System.out.println("Recibida URI tipo GET: " + httpExchange.getRequestURI().toString());
        return httpExchange.getRequestURI().toString();
    }

    /**
     * 
     * @param httpExchange
     * @return
     */
    private String handlePostRequest(HttpExchange httpExchange) {
        System.out.println("Recibida URI tipo POST: " + httpExchange.getRequestBody().toString());
        InputStream is = httpExchange.getRequestBody();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // FIN BLOQUE REQUEST

    // INICIO BLOQUE RESPONSE

    /**
     * 
     * @param httpExchange
     * @throws IOException
     */
    private void handleGetResponseTodos(HttpExchange httpExchange ) throws IOException {

        String json = "";
        String html = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader("delincuentes.json"));
            String line = null;
            while ((line = br.readLine()) != null) {
                json += line + "\n";
            }																	//leer el json y almacena los datos en la variable json
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray delincuentesArray = new JSONArray(json);
        for (int i = 0; i < delincuentesArray.length(); i++) { 					//Recorre y añade los delincuentes al json
            JSONObject delincuentes = delincuentesArray.getJSONObject(i);
            alias = delincuentes.getString("alias");

            html += "<p>Alias: " + alias + "</p><p>---------------------------------</p>";			//Concateno los delincuentes en la variable html
        }

        System.out.println("El servidor pasa a procesar la peticion GET: ");
        //el servidor devuelve al cliente un HTML simple:
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "<html></head><h1>Lista Delincuentes</h1><body>" + html + "</body></html>"; 		//Muestro los delincuentes
        httpExchange.sendResponseHeaders(200, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        System.out.println("Devuelve respuesta HTML: " + htmlResponse);
    }

    /**
     * 
     * @param httpExchange
     * @param requestParamValue
     * @throws IOException
     */
    private void handleGetResponseUno(HttpExchange httpExchange, String requestParamValue) throws IOException {
        String json = "";
        String html = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader("delincuentes.json"));
            String line = null;
            while ((line = br.readLine()) != null) { 							//leer el json y almacena los datos en la variable json
                json += line + "\n";
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray delincuenteArray = new JSONArray(json);
        for (int i = 0; i < delincuenteArray.length(); i++) {
            JSONObject delincuentes = delincuenteArray.getJSONObject(i);
            if (delincuentes.getString("alias").equals(requestParamValue)) { 			//Recorre y añade los delincuentes al json
                alias = delincuentes.getString("alias");
                nombreCompleto = delincuentes.getString("nombreCompleto");
                fechaNacimiento = delincuentes.getString("fechaNacimiento");
                nacionalidad = delincuentes.getString("nacionalidad");
                imagen = delincuentes.getString("imagen");


                html += "<p>Alias: " + alias + "</p> <p>Nombre Completo: " + nombreCompleto + "</p> <p>Fecha Nacimiento: " +
                    fechaNacimiento + "</p> <p>Nacionalidad: " + nacionalidad + "</p> <img src='" + procesarImagen(imagen) + "' width=\"150\" height=\"150\"> ";
                break;																	//Concateno los delincuentes en la variable html
            }
        }

        System.out.println("El servidor pasa a procesar la peticion GET: " + requestParamValue);
        //el servidor devuelve al cliente un HTML simple:
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "<html><body><h1>Datos delicuente: <u>" + requestParamValue + "</u></h1>" + html + "</body></html>"; //Muestro el delincuente

        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        System.out.println("Devuelve respuesta HTML: " + htmlResponse);
    }

    /**
     * 
     * @param httpExchange
     * @throws IOException
     */
    private void handleGetResponseNuevo(HttpExchange httpExchange) throws IOException {

        String html = "";																
        html = "<h1>Nuevo Delincuente</h1><p>Introducir datos</p><form action='http://localhost:5002/servidor/nuevo' method ='post'>" +
            "<label for='alias'>Alias:</label> <input type='text' id='alias' name='alias'><br>" +
            "<label for='nombreCompleto'>Nombre Completo:</label> <input type='text' id='nombreCompleto' name='nombreCompleto'><br>" +
            "<label for='fechaNacimiento'>Fecha Nacimiento:</label> <input type='text' id='fechaNacimiento' name='fechaNacimiento'><br>" +
            "<label for='nacionalidad'>Nacionalidad:</label> <input type='text' id='nacionalidad' name='nacionalidad'><br>" +
            "<label for='imagen'>Imagen:</label> <textarea id='imagen' name='imagen'></textarea><br><input type=\"submit\" value=\"Submit\">";

        System.out.println("El servidor pasa a procesar la peticion GET: ");
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "<html><body>" + html + "</body></html>";						 //Muestro el formulario del html

        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        System.out.println("Devuelve respuesta HTML: " + htmlResponse);
    }

    /**
     * 
     * @param httpExchange
     * @param requestParamValue
     * @throws IOException
     */
    private void handlePostResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {

        System.out.println("El servidor pasa a procesar el body de la peticion POST: " + requestParamValue);
        String input = String.valueOf(requestParamValue);

        String[] pairs = input.split("&");
        Map < String, String > values = new HashMap < > ();
        for (String pair: pairs) { 									//Obtiene los valores del formulario al pulsar el boton Submit del html, el
            String[] keyValue = pair.split("=");					//cual genera un String conjunto que es procesado y obtiene los valores independientes
            values.put(keyValue[0], keyValue[1]);					//a las siguientes variables
        }
        String alias = values.get("alias");
        String nombreCompleto = values.get("nombreCompleto");		
        String fechaNacimiento = values.get("fechaNacimiento");
        String nacionalidad = values.get("nacionalidad");
        String imagen = values.get("imagen");

        boolean existe = false;
        JSONObject datos = new JSONObject(); 							//Meter los valores en el fichero json
        datos.put("alias", alias);
        datos.put("nombreCompleto", nombreCompleto);
        datos.put("fechaNacimiento", fechaNacimiento);
        datos.put("nacionalidad", nacionalidad);
        datos.put("imagen", imagen);

        String filePath = "delincuentes.json";
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            if (obj.getString("alias").equals(alias) && obj.getString("nombreCompleto").equals(nombreCompleto)) { //Si coinciden el alias y el nombre el sujeto ya existe
                existe = true;																					  //lo cual no ingresara al delincuente
                break;
            }
        }
        if (!existe) {
            jsonArray.put(datos); 																				//Si no existe procede a introducir los datos
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(jsonArray.toString());
                file.flush();

                //ENVIO CORREO

                System.out.println("PruebaEmail.java");
                String strMensaje = "Alias: " + alias + "\nNombre Completo: " + nombreCompleto + "\nFecha Nacimiento: " + fechaNacimiento + "\nNacionalidad: " + nacionalidad;
                String strAsunto = "Nuevo delincuente en alta";
                String emailRemitente = "serviciosred977@gmail.com";
                // Scanner teclado = new Scanner(System.in);
                // System.out.print("Introducir contrasenya: ");
                String emailRemitentePass = "xxxx"; //teclado.nextLine(); 
                String hostEmail = "smtp.gmail.com";
                String portEmail = "587";
                String[] emailDestino = {"alpoor@floridauniversitaria.es"};
                String[] anexo = {procesarImagen(imagen)};

                try {
                    envioMail(strMensaje, strAsunto, emailRemitente, emailRemitentePass, hostEmail, portEmail, emailDestino, anexo);

                } catch (UnsupportedEncodingException | MessagingException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // si queremos que el servidor devuelva al cliente un HTML:
        System.out.println("El servidor pasa a procesar la peticion GET: ");
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "<html><body><h1>Datos Enviados Correctamente</h1><button onclick=\"location.href='http://localhost:5002/servidor/nuevo'\">Volver</button></body></html>";

        httpExchange.sendResponseHeaders(200, htmlResponse.length());
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
        System.out.println("Devuelve respuesta HTML: " + htmlResponse);
    }
    // FIN BLOQUE RESPONSE

    
    /**
     * 
     * @param imagen la imagen del delincuente
     * @return la imagen decodificada y procesada para mostrarse adecuadamente
     * @throws UnsupportedEncodingException
     */
    public String procesarImagen(String imagen) throws UnsupportedEncodingException {	//Metodo Procesar imagen
        String encodedString = imagen;
        String decodedString = URLDecoder.decode(encodedString, "UTF-8"); //Decodifica ciertos caracteres el string al ser procesado por el html y cambiar ciertos caracteres especiales

        String imageDataString = decodedString; 						  //Se decodifica la imagen Base64 para poder mostrarla
        byte[] imageByteArray = Base64.getDecoder().decode(imageDataString);

        String imageDataURL = "data:image/jpeg;base64," + imageDataString;
        return imageDataURL;
    }

    //CODIGO ENVIO DE CORREO
    public static void envioMail(String mensaje, String asunto, String email_remitente, String email_remitente_pass, String host_email, String port_email,
        String[] email_destino, String[] anexo) throws UnsupportedEncodingException, MessagingException {

        System.out.println("Envio de correo");
        System.out.println(" > Remitente: " + email_remitente);
        for (int i = 0; i < email_destino.length; i++) {
            System.out.println(" > Destino " + (i + 1) + ": " + email_destino[i]);
        }

        System.out.println(" > Asunto: " + asunto);
        System.out.println(" > Mensaje: " + mensaje);

        Properties props = System.getProperties();
        props.put("mail.smtp.host", host_email);
        props.put("mail.smtp.user", email_remitente);
        props.put("mail.smtp.clave", email_remitente_pass);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", port_email);

        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email_remitente));
        message.addRecipients(Message.RecipientType.TO, email_destino[0]);
        message.setSubject(asunto);

        BodyPart messageBodyPart1 = new MimeBodyPart();
        messageBodyPart1.setText(mensaje);

        BodyPart messageBodyPart2 = new MimeBodyPart();
        DataSource src = new FileDataSource(anexo[0]);
        messageBodyPart2.setDataHandler(new DataHandler(src));
        messageBodyPart2.setFileName(anexo[0]);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart1);
        multipart.addBodyPart(messageBodyPart2);

        message.setContent(multipart);
        Transport transport = session.getTransport("smtp");

        transport.connect(host_email, email_remitente, email_remitente_pass);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}