/* Scanner.java
 *
 * (c) 2008 Salvo Scellato
 *
 * This file is part of the EduMIPS64 project, and is released under the GNU
 * General Public License.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edumips64.core.parser;

import edumips64.core.parser.tokens.*;
import java.io.*;
import java.util.*;

public class Scanner{
    private Reader reader;
    private DirectiveRecognizer directiveRec = new DirectiveRecognizer();
    private NumberRecognizer numRec = new NumberRecognizer();
    private StringRecognizer stringRec = new StringRecognizer();
    private CommentRecognizer commentRec = new CommentRecognizer();
    private RegisterRecognizer regRec = new RegisterRecognizer();
    private IDRecognizer idRec = new IDRecognizer();
    private StringBuilder tempRow = new StringBuilder();
    private int currentLine;
    private int currentColumn;

    private List<String> codeLines = new ArrayList<String>();
    
    /** Constructor.
     * @param reader stream for consuming characters
     */
    public Scanner(Reader reader){
        this.reader = reader;
        this.currentLine = 1;
        this.currentColumn = 1;
    }

    private Token getEOLToken(){
        return new EOLToken(currentLine++,currentColumn);
    }

    /** Returns true only if the scanner has a new token.
     */
    public boolean hasToken(){
        try{
            reader.mark(1);
            int r = reader.read();
            if( r == -1)
                return false;
            else{
                reader.reset();
                return true;
            }
        } catch (IOException e){
            return false;
        }
    }

    // Debug method, prints the actual token
    public Token next() {
        Token t = nextToken();
        System.out.println(t);
        return t;
    }

    public Token nextToken(){
        Token t = null;
        while(true){
            try{
                reader.mark(1);
                int r = reader.read();
                if(r == -1) {
                    codeLines.add(tempRow.toString());
                    return new EOFToken(currentLine, currentColumn);
                }

                char token = (char) r;
                
                //Pay attention, newline character is recognized as
                //whitespace...!
                if( token == '\n'){
                    currentColumn = 1;
                    codeLines.add(tempRow.toString());
                    tempRow.setLength(0);        // empty the StringBuilder
                    return getEOLToken();
                }

                if( Character.isWhitespace(token)){
                    currentColumn++;
                    tempRow.append(token);
                    continue;
                }
                
                //registers and IDs start with a letter
                if( Character.isLetter(token)){
                    reader.reset();
                    //may be an identifier or a register
                    t = idRec.recognize(reader);
                }
                //numbers (integer and float) start with a digit or + and -
                else if( Character.isDigit(token) || token == '+' || token == '-'){
                    reader.reset();
                    t = numRec.recognize(reader);
                }else{
                    //single characters, comment, strings and directives
                    //only start with certain characters
                    switch(token){
                        case '(': t = new LeftParenToken(); break;      
                        case ')': t = new RightParenToken(); break;      
                        case ',': t = new CommaToken(); break;
                        case ':': t = new ColonToken(); break;
                        case '#': t = numRec.recognize(reader); break;
                        case ';': reader.reset();
                                  t = commentRec.recognize(reader);
                                  break;
                        case '$': reader.reset();
                                  t = regRec.recognize(reader);
                                  break;
                        case '.': reader.reset(); 
                                  t = directiveRec.recognize(reader);
                                  break;
                        case '"': reader.reset();
                                  t = stringRec.recognize(reader);
                                  break;
                        default: t = new ErrorToken(""+token,currentLine, currentColumn);
                    }
                }

                if( t == null)
                    t = new ErrorToken(""+token,currentLine, currentColumn);
                
                tempRow.append(t.getBuffer());

                t.setLine(currentLine);
                t.setColumn(currentColumn);
                currentColumn += t.getBuffer().length();
                return t;
            } catch (IOException e){
                return new ErrorToken("I/O Error", currentLine);
            }
        }
    }

    public String getLastCodeLine() {
        int pos = codeLines.size() - 1;
        if(pos >= 0)
            return codeLines.get(pos);
        return "";
    }

    public static void main(String[] args) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        Scanner scanner = new Scanner(reader);
        while(scanner.hasToken())
            edumips64.Main.logger.debug(scanner.nextToken().toString());
    }
}

