package engine;

import java.util.ArrayList;
import java.util.List;

import model.Card;
import model.CardType;
import ai.ComputerPlayer;
import model.Deck;
import model.Phase;
import model.Player;
import model.Turn;

//Handles saving and loading the game state to and from a file.
public class GameSerializer{

    public String serialize(GameState state) {
        if (state == null) return "";

        StringBuilder sb = new StringBuilder();

        sb.append("v1").append("\n");

        sb.append("phase=").append(state.getPhase() == null ? "MENU" : state.getPhase().name()).append("\n");
        sb.append("turn=").append(state.getTurn() == null ? "PLAYER" : state.getTurn().name()).append("\n");
        sb.append("round=").append(state.getRoundNumber()).append("\n");

        sb.append("skipP=").append(state.isSkipPlayerNextTurn()).append("\n");
        sb.append("skipC=").append(state.isSkipComputerNextTurn()).append("\n");

        sb.append("pName=").append(escape(state.getPlayer().getName())).append("\n");
        sb.append("pScore=").append(state.getPlayer().getScore()).append("\n");
        sb.append("pEnergy=").append(state.getPlayer().getEnergy()).append("\n");

        sb.append("cName=").append(escape(state.getComputer().getName())).append("\n");
        sb.append("cScore=").append(state.getComputer().getScore()).append("\n");
        sb.append("cEnergy=").append(state.getComputer().getEnergy()).append("\n");

        sb.append("pHand=").append(serializeHand(state.getPlayer().getHand())).append("\n");
        sb.append("cHand=").append(serializeHand(state.getComputer().getHand())).append("\n");
        sb.append("deck=").append(serializeHand(state.getDeck().peekAll())).append("\n");

        return sb.toString();
    }

    public GameState deserialize(String text) {
        if (text == null) return null;
        String[] lines = text.split("\n");
        if (lines.length == 0) return null;

        int idx = 0;
        String header = lines[idx++].trim();
        if (!"v1".equals(header)) return null;

        String phaseS = "MENU";
        String turnS = "PLAYER";
        int round = 1;
        boolean skipP = false;
        boolean skipC = false;

        String pName = "Player";
        int pScore = 0;
        int pEnergy = 0;

        String cName = "Computer";
        int cScore = 0;
        int cEnergy = 0;

        List<Card> pHand = new ArrayList<>();
        List<Card> cHand = new ArrayList<>();
        List<Card> deckCards = new ArrayList<>();

        while (idx < lines.length) {
            String line = lines[idx++].trim();
            if (line.isEmpty()) continue;

            int eq = line.indexOf('=');
            if (eq < 0) continue;

            String key = line.substring(0, eq).trim();
            String val = line.substring(eq + 1).trim();

            if ("phase".equals(key)) phaseS = val;
            else if ("turn".equals(key)) turnS = val;
            else if ("round".equals(key)) round = parseInt(val, 1);
            else if ("skipP".equals(key)) skipP = parseBool(val);
            else if ("skipC".equals(key)) skipC = parseBool(val);

            else if ("pName".equals(key)) pName = unescape(val);
            else if ("pScore".equals(key)) pScore = parseInt(val, 0);
            else if ("pEnergy".equals(key)) pEnergy = parseInt(val, 0);

            else if ("cName".equals(key)) cName = unescape(val);
            else if ("cScore".equals(key)) cScore = parseInt(val, 0);
            else if ("cEnergy".equals(key)) cEnergy = parseInt(val, 0);

            else if ("pHand".equals(key)) pHand = deserializeHand(val);
            else if ("cHand".equals(key)) cHand = deserializeHand(val);
            else if ("deck".equals(key)) deckCards = deserializeHand(val);
        }

        Player player = new Player(pName);
        ComputerPlayer computer = new ComputerPlayer(cName, (e, h) -> (h == null || h.isEmpty()) ? null : h.get(0));
        Deck deck = new Deck();
        
        deck.clear();
        if (deckCards != null) 
        {
            for (Card c : deckCards) deck.addToTop(c); 
        }

        GameState state = new GameState(player, computer, deck);

        state.setPhase(parsePhase(phaseS));
        state.setTurn(parseTurn(turnS));
        state.setRoundNumber(round);
        state.setSkipPlayerNextTurn(skipP);
        state.setSkipComputerNextTurn(skipC);

        player.setScore(pScore);
        player.setEnergy(pEnergy);
        computer.setScore(cScore);
        computer.setEnergy(cEnergy);

        player.clearHand();
        player.addCards(pHand);
        computer.clearHand();
        computer.addCards(cHand);

        return state;
    }

    private String serializeHand(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c == null) continue;
            if (sb.length() > 0) sb.append(",");
            sb.append(escape(cardToToken(c)));
        }
        return sb.toString();
    }

    private List<Card> deserializeHand(String s) {
        List<Card> list = new ArrayList<>();
        if (s == null || s.trim().isEmpty()) return list;

        String[] parts = s.split(",");
        for (String raw : parts) {
            String token = unescape(raw.trim());
            Card c = tokenToCard(token);
            if (c != null) list.add(c);
        }
        return list;
    }

    private String cardToToken(Card c) {
        String id = safe(c.getId());
        String name = safe(c.getName());
        String type = (c.getType() == null) ? CardType.FIRE.name() : c.getType().name();
        int atk = c.getAttack();
        int def = c.getDefense();
        int cost = c.getCost();
        String img = safe(c.getImagePath());
        return id + "|" + name + "|" + type + "|" + atk + "|" + def + "|" + cost + "|" + img;
    }

    private Card tokenToCard(String token) {
        if (token == null || token.isEmpty()) return null;

        String[] p = token.split("\\|", -1);
        if (p.length < 7) return null;

        String id = p[0];
        String name = p[1];
        CardType type = parseType(p[2]);
        int atk = parseInt(p[3], 0);
        int def = parseInt(p[4], 0);
        int cost = parseInt(p[5], 0);
        String img = p[6];
        return new Card(id, name, type, atk, def, cost, img);
    }

    private CardType parseType(String s) {
        try {
            return CardType.valueOf(s);
        } catch (Exception ex) {
            return CardType.FIRE;
        }
    }

    private Phase parsePhase(String s) {
        try {
            return Phase.valueOf(s);
        } catch (Exception ex) {
            return Phase.MENU;
        }
    }

    private Turn parseTurn(String s) {
        try {
            return Turn.valueOf(s);
        } catch (Exception ex) {
            return Turn.PLAYER;
        }
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return def;
        }
    }

    private boolean parseBool(String s) {
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t").replace("|", "\\p").replace(",", "\\c").replace("=", "\\e");
    }

    private String unescape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!esc) {
                if (ch == '\\') esc = true;
                else out.append(ch);
            } else {
                if (ch == 'n') out.append('\n');
                else if (ch == 't') out.append('\t');
                else if (ch == 'p') out.append('|');
                else if (ch == 'c') out.append(',');
                else if (ch == 'e') out.append('=');
                else out.append(ch);
                esc = false;
            }
        }
        if (esc) out.append('\\');
        return out.toString();
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
