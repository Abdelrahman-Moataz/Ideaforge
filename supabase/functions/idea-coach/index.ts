// Supabase Edge Function: idea-coach
// Configure GEMINI_API_KEY as a Supabase Function secret. Never put it in the Android app.
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return Response.json({ error: "Method not allowed." }, { status: 405, headers: corsHeaders });
  const apiKey = Deno.env.get("GEMINI_API_KEY");
  if (!apiKey) return Response.json({ error: "AI service is not configured. Add GEMINI_API_KEY to Supabase Function secrets." }, { status: 500, headers: corsHeaders });
  try {
    const body = await req.json();
    const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
    const ideaContext = typeof body.ideaContext === "string" ? body.ideaContext.trim() : "";
    if (!prompt || prompt.length > 4000) return Response.json({ error: "Enter a prompt of 1–4000 characters." }, { status: 400, headers: corsHeaders });
    const systemInstruction = "You are IdeaForge’s practical startup idea coach. Be clear, constructive, specific, and realistic. Separate assumptions from known facts. Do not claim to have verified current market size, competitors, or demand unless reliable evidence is provided. Never guarantee success. Prefer concise headings and numbered actions. When relevant, cover customer, problem, current alternatives, MVP, validation experiments, risks, and monetization. Help the user test assumptions rather than simply praise the idea.";
    const userText = ideaContext ? "Idea context:\n" + ideaContext + "\n\nUser request:\n" + prompt : prompt;
    const aiResponse = await fetch("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent", {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": apiKey },
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: systemInstruction }] },
        contents: [{ role: "user", parts: [{ text: userText }] }],
        generationConfig: { temperature: 0.6, maxOutputTokens: 1200 },
      }),
    });
    const data = await aiResponse.json();
    if (!aiResponse.ok) {
      console.error("Gemini API request failed:", aiResponse.status, JSON.stringify(data).slice(0, 1000));
      return Response.json({ error: "The AI provider could not complete the request. Check the Gemini API key and quota." }, { status: 502, headers: corsHeaders });
    }
    const text = data?.candidates?.[0]?.content?.parts?.map((part: { text?: string }) => part.text ?? "").join("\n").trim();
    if (!text) return Response.json({ error: "The AI provider returned no text. Try a different prompt." }, { status: 502, headers: corsHeaders });
    return Response.json({ text }, { headers: corsHeaders });
  } catch (error) {
    console.error("idea-coach error:", error);
    return Response.json({ error: "Unable to process the AI request." }, { status: 500, headers: corsHeaders });
  }
});
