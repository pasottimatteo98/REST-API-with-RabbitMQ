import java.net.URI;

public class Request {

    private String Type;
    private URI URL;
    private String Query;

    public Request(String Type, URI URL, String Query){
        this.Type=Type;
        this.URL=URL;
        this.Query=Query;
    }
    
    public String getType(){
        return Type;
    }

    public URI getURL(){
        return URL;
    }

    public String getQuery(){
        return Query;
    }

    public String toString(){
        return "Type = "+ Type + " URL = "+ URL + " Query = "+ Query;
    }
}
