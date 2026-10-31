// Supabase Edge Function: idea-coach
// Configure OPENROUTER_API_KEY in Supabase Function secrets.
// Never place provider keys in Flutter, Android, or source control.
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type, x-client-info",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return Response.json({ error: "Method not allowed." }, { status: 405, headers: corsHeaders });

  const apiKey = Deno.env.get("OPENROUTER_API_KEY");
  if (!apiKey) {
    return Response.json({ error: "AI service is not configured. Add OPENROUTER_API_KEY to Supabase Function secrets." }, { status: 500, headers: corsHeaders });
  }

  try {
    const body = await req.json();
    const prompt = typeof body.prompt === "string" ? body.prompt.trim() : "";
    const ideaContext = typeof body.ideaContext === "string" ? body.ideaContext.trim() : "";
    if (!prompt || prompt.length > 4000) {
      return Response.json({ error: "Enter a prompt of 1–4000 characters." }, { status: 400, headers: corsHeaders });
    }

    const systemPrompt = "You are IdeaForge's practical startup idea coach. Be clear, constructive, specific, and realistic. Separate assumptions from known facts. Do not claim to verify current market size, competitors, or demand without reliable evidence. Never guarantee success. Prefer concise headings and numbered actions. When relevant, cover customer, problem, alternatives, MVP, validation experiments, risks, and monetization. Help the user test assumptions rather than simply praise the idea.";
    const userPrompt = ideaContext ? "Idea context:\n" + ideaContext + "\n\nUser request:\n" + prompt : prompt;

    const upstream = await fetch("https://openrouter.ai/api/v1/chat/completions", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "HTTP-Referer": "https://ideaforge.app",
        "X-Title": "IdeaForge",
      },
      body: JSON.stringify({
        model: "nvidia/nemotron-3.5-super-vl:free",
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: userPrompt },
        ],
        temperature: 0.6,
        max_tokens: 1400,
      }),
    });

    const data = await upstream.json();
    if (!upstream.ok) {
      console.error("OpenRouter request failed:", upstream.status, JSON.stringify(data).slice(0, 1200));
      return Response.json({ error: "The AI provider could not complete the request. Check the OpenRouter key, model availability, and provider limits." }, { status: 502, headers: corsHeaders });
    }

    const answer = data?.choices?.[0]?.message?.content;
    const text = typeof answer === "string"
      ? answer.trim()
      : Array.isArray(answer)
        ? answer.map((part: { text?: string }) => part.text ?? "").join("\n").trim()
        : "";
    if (!text) return Response.json({ error: "The AI provider returned no text. Try again." }, { status: 502, headers: corsHeaders });

    return Response.json({ text }, { headers: corsHeaders });
  } catch (error) {
    console.error("idea-coach error:", error);
    return Response.json({ error: "Unable to process the AI request." }, { status: 500, headers: corsHeaders });
  }
});
