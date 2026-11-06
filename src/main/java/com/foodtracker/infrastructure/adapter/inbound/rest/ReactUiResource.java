package com.foodtracker.infrastructure.adapter.inbound.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ReactUiResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getIndex() {
        return getReactUi();
    }

    @GET
    @Path("/ui")
    @Produces(MediaType.TEXT_HTML)
    public Response getReactUi() {
        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FoodTracker - Test UI</title>
  <script src="https://unpkg.com/react@18/umd/react.production.min.js" crossorigin></script>
  <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js" crossorigin></script>
  <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script src="https://cdn.tailwindcss.com"></script>
  <!-- Google Sign-In Library -->
  <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>
<body class="bg-slate-50 text-slate-800 antialiased min-h-screen">
  <div id="root"></div>

  <script type="text/babel">
    const { useState, useEffect } = React;

    function App() {
      const [token, setToken] = useState(localStorage.getItem('ft_token') || 'dev-token:alice@example.com:Alice Smith');
      const [user, setUser] = useState(null);
      const [format, setFormat] = useState('json'); // 'json' or 'xml'
      const [foods, setFoods] = useState([]);
      const [reactions, setReactions] = useState([]);
      const [insights, setInsights] = useState([]);
      const [selectedReaction, setSelectedReaction] = useState(null);
      const [correlationData, setCorrelationData] = useState(null);
      const [statusMsg, setStatusMsg] = useState({ text: '', isError: false });

      // Form States
      const [foodName, setFoodName] = useState('');
      const [mealType, setMealType] = useState('LUNCH');
      const [ingredients, setIngredients] = useState('');
      const [consumedAt, setConsumedAt] = useState(new Date().toISOString().slice(0, 16));
      const [foodNotes, setFoodNotes] = useState('');

      const [symptom, setSymptom] = useState('');
      const [severity, setSeverity] = useState('MODERATE');
      const [occurredAt, setOccurredAt] = useState(new Date().toISOString().slice(0, 16));
      const [reactionNotes, setReactionNotes] = useState('');

      const showStatus = (text, isError = false) => {
        setStatusMsg({ text, isError });
        setTimeout(() => setStatusMsg({ text: '', isError: false }), 4500);
      };

      const getHeaders = (isXml = false) => {
        const ct = isXml ? 'application/xml' : 'application/json';
        const h = { 'Accept': ct, 'Content-Type': ct };
        if (token) {
          h['Authorization'] = `Bearer ${token}`;
        }
        return h;
      };

      // Authenticate / fetch user
      const loadProfile = async (authToken) => {
        try {
          const res = await fetch('/api/v1/auth/me', {
            headers: { 'Authorization': `Bearer ${authToken || token}`, 'Accept': 'application/json' }
          });
          if (res.ok) {
            const data = await res.json();
            setUser(data);
            showStatus(`Logged in as ${data.name || data.email}`);
            loadAllData(authToken || token);
          } else {
            setUser(null);
          }
        } catch (err) {
          console.error(err);
        }
      };

      const handleDevLogin = async (email, name) => {
        const devToken = `dev-token:${email}:${name}`;
        setToken(devToken);
        localStorage.setItem('ft_token', devToken);
        const res = await fetch('/api/v1/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
          body: JSON.stringify({ email, name })
        });
        if (res.ok) {
          const data = await res.json();
          setUser(data);
          showStatus(`Dev login: ${data.name}`);
          loadAllData(devToken);
        }
      };

      const loadAllData = (authToken) => {
        fetchFoods(authToken);
        fetchReactions(authToken);
        fetchInsights(authToken);
      };

      const fetchFoods = async (authToken) => {
        try {
          const res = await fetch('/api/v1/foods', {
            headers: { 'Authorization': `Bearer ${authToken || token}`, 'Accept': 'application/json' }
          });
          if (res.ok) setFoods(await res.json());
        } catch (e) { console.error(e); }
      };

      const fetchReactions = async (authToken) => {
        try {
          const res = await fetch('/api/v1/reactions', {
            headers: { 'Authorization': `Bearer ${authToken || token}`, 'Accept': 'application/json' }
          });
          if (res.ok) setReactions(await res.json());
        } catch (e) { console.error(e); }
      };

      const fetchInsights = async (authToken) => {
        try {
          const res = await fetch('/api/v1/analytics/correlations?lookbackHours=72', {
            headers: { 'Authorization': `Bearer ${authToken || token}`, 'Accept': 'application/json' }
          });
          if (res.ok) setInsights(await res.json());
        } catch (e) { console.error(e); }
      };

      useEffect(() => {
        if (token) {
          loadProfile(token);
        }
      }, []);

      const handleAddFood = async (e) => {
        e.preventDefault();
        if (!foodName.trim()) return;

        let bodyPayload;
        const isoConsumed = new Date(consumedAt).toISOString();
        const useXml = format === 'xml';

        if (useXml) {
          bodyPayload = `<createFoodEntry><foodName>${foodName}</foodName><mealType>${mealType}</mealType><ingredients>${ingredients}</ingredients><consumedAt>${isoConsumed}</consumedAt><notes>${foodNotes}</notes></createFoodEntry>`;
        } else {
          bodyPayload = JSON.stringify({
            foodName,
            mealType,
            ingredients,
            consumedAt: isoConsumed,
            notes: foodNotes
          });
        }

        try {
          const res = await fetch('/api/v1/foods', {
            method: 'POST',
            headers: getHeaders(useXml),
            body: bodyPayload
          });
          if (res.ok) {
            showStatus(`Logged food '${foodName}' (${format.toUpperCase()})`);
            setFoodName('');
            setIngredients('');
            setFoodNotes('');
            fetchFoods();
            fetchInsights();
          } else {
            const err = await res.text();
            showStatus(`Error logging food: ${err}`, true);
          }
        } catch (err) {
          showStatus(`Request failed: ${err.message}`, true);
        }
      };

      const handleDeleteFood = async (id) => {
        const res = await fetch(`/api/v1/foods/${id}`, {
          method: 'DELETE',
          headers: getHeaders()
        });
        if (res.ok) {
          showStatus('Food entry deleted');
          fetchFoods();
          fetchInsights();
        }
      };

      const handleAddReaction = async (e) => {
        e.preventDefault();
        if (!symptom.trim()) return;

        let bodyPayload;
        const isoOccurred = new Date(occurredAt).toISOString();
        const useXml = format === 'xml';

        if (useXml) {
          bodyPayload = `<createAdverseReaction><symptom>${symptom}</symptom><severity>${severity}</severity><occurredAt>${isoOccurred}</occurredAt><notes>${reactionNotes}</notes></createAdverseReaction>`;
        } else {
          bodyPayload = JSON.stringify({
            symptom,
            severity,
            occurredAt: isoOccurred,
            notes: reactionNotes
          });
        }

        try {
          const res = await fetch('/api/v1/reactions', {
            method: 'POST',
            headers: getHeaders(useXml),
            body: bodyPayload
          });
          if (res.ok) {
            showStatus(`Logged symptom '${symptom}' (${format.toUpperCase()})`);
            setSymptom('');
            setReactionNotes('');
            fetchReactions();
            fetchInsights();
          } else {
            const err = await res.text();
            showStatus(`Error logging reaction: ${err}`, true);
          }
        } catch (err) {
          showStatus(`Request failed: ${err.message}`, true);
        }
      };

      const handleDeleteReaction = async (id) => {
        const res = await fetch(`/api/v1/reactions/${id}`, {
          method: 'DELETE',
          headers: getHeaders()
        });
        if (res.ok) {
          showStatus('Adverse reaction deleted');
          if (selectedReaction && selectedReaction.id === id) {
            setSelectedReaction(null);
            setCorrelationData(null);
          }
          fetchReactions();
          fetchInsights();
        }
      };

      const handleInspectSuspectFoods = async (reaction) => {
        setSelectedReaction(reaction);
        try {
          const res = await fetch(`/api/v1/reactions/${reaction.id}/suspect-foods?lookbackHours=72`, {
            headers: getHeaders()
          });
          if (res.ok) {
            const data = await res.json();
            setCorrelationData(data);
          }
        } catch (e) {
          console.error(e);
        }
      };

      return (
        <div class="max-w-7xl mx-auto px-4 py-8">
          {/* Header */}
          <header class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mb-8 flex flex-wrap items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-3">
                <span class="text-3xl">🥗</span>
                <div>
                  <h1 class="text-2xl font-bold text-slate-900">FoodTracker WebAPI</h1>
                  <p class="text-sm text-slate-500">Hexagonal Architecture &bull; SQLite &bull; Hibernate ORM &bull; Google OAuth2</p>
                </div>
              </div>
            </div>

            <div class="flex items-center gap-3">
              {/* Format Toggle */}
              <div class="flex items-center bg-slate-100 rounded-lg p-1 border border-slate-200 text-xs font-semibold">
                <button
                  onClick={() => setFormat('json')}
                  class={`px-3 py-1.5 rounded-md transition ${format === 'json' ? 'bg-white shadow text-emerald-700' : 'text-slate-600'}`}>
                  JSON
                </button>
                <button
                  onClick={() => setFormat('xml')}
                  class={`px-3 py-1.5 rounded-md transition ${format === 'xml' ? 'bg-white shadow text-emerald-700' : 'text-slate-600'}`}>
                  XML
                </button>
              </div>

              <a href="/swagger" target="_blank"
                 class="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-medium rounded-lg shadow-sm flex items-center gap-1.5 transition">
                <span>Swagger UI</span>
                <span class="text-xs">&rarr;</span>
              </a>
            </div>
          </header>

          {/* Status Message */}
          {statusMsg.text && (
            <div class={`mb-6 p-4 rounded-lg text-sm font-medium ${statusMsg.isError ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-emerald-50 text-emerald-700 border border-emerald-200'}`}>
              {statusMsg.text}
            </div>
          )}

          {/* Auth Bar */}
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-5 mb-8 flex flex-wrap items-center justify-between gap-4">
            <div class="flex items-center gap-3">
              {user ? (
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full bg-emerald-100 text-emerald-800 font-bold flex items-center justify-center border border-emerald-300">
                    {user.name ? user.name[0].toUpperCase() : 'U'}
                  </div>
                  <div>
                    <div class="font-semibold text-slate-900">{user.name}</div>
                    <div class="text-xs text-slate-500">{user.email} &bull; ID: {user.id.slice(0, 8)}...</div>
                  </div>
                </div>
              ) : (
                <div class="text-sm text-slate-500">Not authenticated. Login below to test protected endpoints:</div>
              )}
            </div>

            <div class="flex items-center gap-2 flex-wrap">
              <button
                onClick={() => handleDevLogin('alice@example.com', 'Alice Smith')}
                class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md border border-slate-300 transition">
                Dev Login (Alice)
              </button>
              <button
                onClick={() => handleDevLogin('bob@example.com', 'Bob Johnson')}
                class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md border border-slate-300 transition">
                Dev Login (Bob)
              </button>
              <div class="flex items-center gap-1">
                <input
                  type="text"
                  placeholder="Bearer Token..."
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  class="text-xs px-2.5 py-1.5 border border-slate-300 rounded-md w-48 font-mono"
                />
                <button
                  onClick={() => loadProfile(token)}
                  class="px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-medium rounded-md border border-indigo-200">
                  Set
                </button>
              </div>
            </div>
          </div>

          {/* Main Grid: Food Logging & Reaction Tracking */}
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
            {/* Food Section */}
            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
              <div>
                <div class="flex items-center justify-between mb-4">
                  <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                    <span>🍲</span> Log Food Consumption
                  </h2>
                  <span class="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-mono">POST /api/v1/foods</span>
                </div>

                <form onSubmit={handleAddFood} class="space-y-4 mb-6">
                  <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Food Name *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Scrambled Eggs, Shrimp Risotto, Almond Milk"
                      value={foodName}
                      onChange={(e) => setFoodName(e.target.value)}
                      class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                  </div>

                  <div class="grid grid-cols-2 gap-3">
                    <div>
                      <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Meal Type</label>
                      <select
                        value={mealType}
                        onChange={(e) => setMealType(e.target.value)}
                        class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none">
                        <option value="BREAKFAST">Breakfast</option>
                        <option value="LUNCH">Lunch</option>
                        <option value="DINNER">Dinner</option>
                        <option value="SNACK">Snack</option>
                        <option value="BEVERAGE">Beverage</option>
                        <option value="OTHER">Other</option>
                      </select>
                    </div>

                    <div>
                      <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Consumed At</label>
                      <input
                        type="datetime-local"
                        value={consumedAt}
                        onChange={(e) => setConsumedAt(e.target.value)}
                        class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                      />
                    </div>
                  </div>

                  <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Ingredients (comma-separated)</label>
                    <input
                      type="text"
                      placeholder="e.g. wheat, dairy, eggs, soy, shellfish"
                      value={ingredients}
                      onChange={(e) => setIngredients(e.target.value)}
                      class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                  </div>

                  <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Notes</label>
                    <input
                      type="text"
                      placeholder="e.g. Restaurant cooked in butter"
                      value={foodNotes}
                      onChange={(e) => setFoodNotes(e.target.value)}
                      class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                  </div>

                  <button
                    type="submit"
                    class="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg shadow-sm transition">
                    + Log Food ({format.toUpperCase()})
                  </button>
                </form>

                {/* Quick Samples */}
                <div class="flex items-center gap-2 mb-4 text-xs text-slate-500 flex-wrap">
                  <span>Quick sample:</span>
                  <button type="button" onClick={() => { setFoodName('Peanut Butter Toast'); setMealType('BREAKFAST'); setIngredients('peanuts, wheat, yeast'); }} class="text-emerald-700 hover:underline">Peanut Butter Toast</button> &bull;
                  <button type="button" onClick={() => { setFoodName('Clam Chowder'); setMealType('LUNCH'); setIngredients('clams, cream, celery, milk'); }} class="text-emerald-700 hover:underline">Clam Chowder</button> &bull;
                  <button type="button" onClick={() => { setFoodName('Milk Chocolate Bar'); setMealType('SNACK'); setIngredients('milk, cocoa, sugar, soy lecithin'); }} class="text-emerald-700 hover:underline">Chocolate</button>
                </div>
              </div>

              {/* Food List */}
              <div class="mt-4 border-t border-slate-100 pt-4">
                <div class="flex items-center justify-between mb-3">
                  <h3 class="text-sm font-bold text-slate-700">Logged Foods ({foods.length})</h3>
                  <button onClick={() => fetchFoods()} class="text-xs text-emerald-600 hover:underline">Refresh</button>
                </div>

                <div class="max-h-72 overflow-y-auto space-y-2 pr-1">
                  {foods.length === 0 ? (
                    <div class="text-center py-6 text-sm text-slate-400">No foods logged yet.</div>
                  ) : (
                    foods.map(f => (
                      <div key={f.id} class="p-3 bg-slate-50 rounded-lg border border-slate-200 flex items-start justify-between gap-2">
                        <div>
                          <div class="font-semibold text-sm text-slate-800">{f.foodName}</div>
                          <div class="text-xs text-slate-500 flex items-center gap-2 mt-0.5">
                            <span class="px-1.5 py-0.5 bg-slate-200 rounded text-[11px] font-medium">{f.mealType}</span>
                            <span>{new Date(f.consumedAt).toLocaleString()}</span>
                          </div>
                          {f.ingredients && <div class="text-xs text-slate-600 mt-1"><span class="font-medium">Ingredients:</span> {f.ingredients}</div>}
                        </div>
                        <button
                          onClick={() => handleDeleteFood(f.id)}
                          class="text-xs text-red-500 hover:text-red-700 px-2 py-1 rounded hover:bg-red-50">
                          Delete
                        </button>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>

            {/* Adverse Reactions Section */}
            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
              <div>
                <div class="flex items-center justify-between mb-4">
                  <h2 class="text-lg font-bold text-slate-900 flex items-center gap-2">
                    <span>⚠️</span> Log Adverse Reaction / Symptom
                  </h2>
                  <span class="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-mono">POST /api/v1/reactions</span>
                </div>

                <form onSubmit={handleAddReaction} class="space-y-4 mb-6">
                  <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Symptom / Adverse Effect *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Skin Rash, Stomach Cramps, Migraine, Bloating"
                      value={symptom}
                      onChange={(e) => setSymptom(e.target.value)}
                      class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                    />
                  </div>

                  <div class="grid grid-cols-2 gap-3">
                    <div>
                      <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Severity</label>
                      <select
                        value={severity}
                        onChange={(e) => setSeverity(e.target.value)}
                        class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none">
                        <option value="MILD">Mild</option>
                        <option value="MODERATE">Moderate</option>
                        <option value="SEVERE">Severe</option>
                      </select>
                    </div>

                    <div>
                      <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Occurred At</label>
                      <input
                        type="datetime-local"
                        value={occurredAt}
                        onChange={(e) => setOccurredAt(e.target.value)}
                        class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                      />
                    </div>
                  </div>

                  <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase mb-1">Notes</label>
                    <input
                      type="text"
                      placeholder="e.g. Started 2 days after dinner, lasted 4 hours"
                      value={reactionNotes}
                      onChange={(e) => setReactionNotes(e.target.value)}
                      class="w-full text-sm px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                    />
                  </div>

                  <button
                    type="submit"
                    class="w-full py-2 bg-amber-600 hover:bg-amber-700 text-white font-medium text-sm rounded-lg shadow-sm transition">
                    + Log Adverse Reaction ({format.toUpperCase()})
                  </button>
                </form>

                {/* Quick Symptom Samples */}
                <div class="flex items-center gap-2 mb-4 text-xs text-slate-500 flex-wrap">
                  <span>Quick sample:</span>
                  <button type="button" onClick={() => { setSymptom('Severe Bloating'); setSeverity('MODERATE'); }} class="text-amber-700 hover:underline">Severe Bloating</button> &bull;
                  <button type="button" onClick={() => { setSymptom('Skin Hives & Itching'); setSeverity('SEVERE'); }} class="text-amber-700 hover:underline">Hives / Rash</button> &bull;
                  <button type="button" onClick={() => { setSymptom('Acute Migraine'); setSeverity('SEVERE'); }} class="text-amber-700 hover:underline">Migraine</button>
                </div>
              </div>

              {/* Reaction List */}
              <div class="mt-4 border-t border-slate-100 pt-4">
                <div class="flex items-center justify-between mb-3">
                  <h3 class="text-sm font-bold text-slate-700">Logged Reactions ({reactions.length})</h3>
                  <button onClick={() => fetchReactions()} class="text-xs text-amber-600 hover:underline">Refresh</button>
                </div>

                <div class="max-h-72 overflow-y-auto space-y-2 pr-1">
                  {reactions.length === 0 ? (
                    <div class="text-center py-6 text-sm text-slate-400">No adverse reactions reported yet.</div>
                  ) : (
                    reactions.map(r => {
                      const badgeColor = r.severity === 'SEVERE' ? 'bg-red-100 text-red-800' :
                                         r.severity === 'MODERATE' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800';
                      return (
                        <div key={r.id} class="p-3 bg-slate-50 rounded-lg border border-slate-200">
                          <div class="flex items-start justify-between gap-2">
                            <div>
                              <div class="font-semibold text-sm text-slate-800">{r.symptom}</div>
                              <div class="text-xs text-slate-500 flex items-center gap-2 mt-0.5">
                                <span class={`px-1.5 py-0.5 rounded text-[11px] font-medium ${badgeColor}`}>{r.severity}</span>
                                <span>{new Date(r.occurredAt).toLocaleString()}</span>
                              </div>
                              {r.notes && <div class="text-xs text-slate-600 mt-1">{r.notes}</div>}
                            </div>
                            <div class="flex items-center gap-1">
                              <button
                                onClick={() => handleInspectSuspectFoods(r)}
                                class="text-xs font-semibold px-2.5 py-1 rounded bg-indigo-50 text-indigo-700 hover:bg-indigo-100">
                                Correlate Foods &rarr;
                              </button>
                              <button
                                onClick={() => handleDeleteReaction(r.id)}
                                class="text-xs text-red-500 hover:text-red-700 px-2 py-1 rounded hover:bg-red-50">
                                &times;
                              </button>
                            </div>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Correlation & Lookback Analysis Modal/Card */}
          {selectedReaction && correlationData && (
            <div class="bg-white rounded-xl shadow-md border-2 border-indigo-200 p-6 mb-8">
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-3">
                  <span class="text-2xl">🔍</span>
                  <div>
                    <h3 class="text-lg font-bold text-slate-900">
                      Food Correlation Analysis for: <span class="text-indigo-700">{selectedReaction.symptom}</span>
                    </h3>
                    <p class="text-xs text-slate-500">
                      Occurred on {new Date(selectedReaction.occurredAt).toLocaleString()} &bull; Lookback window: {correlationData.lookbackHours} hours (3 days)
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => { setSelectedReaction(null); setCorrelationData(null); }}
                  class="text-slate-400 hover:text-slate-600 font-bold text-lg">
                  &times;
                </button>
              </div>

              <div class="mt-4">
                <h4 class="text-xs font-bold uppercase tracking-wider text-slate-600 mb-2">
                  Foods consumed within the {correlationData.lookbackHours} hours prior:
                </h4>

                {(!correlationData.precedingFoods || correlationData.precedingFoods.length === 0) ? (
                  <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-500 text-center">
                    No foods were logged in the {correlationData.lookbackHours} hours before this reaction.
                  </div>
                ) : (
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {correlationData.precedingFoods.map((food, idx) => (
                      <div key={idx} class="p-3 bg-indigo-50/50 rounded-lg border border-indigo-100">
                        <div class="font-bold text-sm text-slate-800">{food.foodName}</div>
                        <div class="text-xs text-slate-500 mt-1">
                          Consumed: {new Date(food.consumedAt).toLocaleString()}
                        </div>
                        {food.ingredients && (
                          <div class="text-xs text-indigo-900 mt-1">
                            <span class="font-medium">Ingredients:</span> {food.ingredients}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Aggregated Insights Section */}
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-2">
                <span class="text-2xl">📊</span>
                <div>
                  <h3 class="text-lg font-bold text-slate-900">Aggregated Food Trigger Patterns</h3>
                  <p class="text-xs text-slate-500">Frequency of foods consumed within 72 hours across all logged reactions</p>
                </div>
              </div>
              <button onClick={() => fetchInsights()} class="text-xs text-emerald-600 hover:underline">Refresh Analysis</button>
            </div>

            {insights.length === 0 ? (
              <div class="p-8 text-center text-sm text-slate-400">
                Log meals and symptoms over multiple days to automatically identify potential food triggers and adverse correlations.
              </div>
            ) : (
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {insights.map((item, idx) => (
                  <div key={idx} class="p-4 bg-slate-50 rounded-lg border border-slate-200">
                    <div class="flex items-center justify-between mb-1">
                      <span class="font-bold text-slate-900">{item.foodName}</span>
                      <span class="text-xs font-bold px-2 py-0.5 rounded-full bg-red-100 text-red-700">
                        {item.occurrenceCount} {item.occurrenceCount === 1 ? 'incident' : 'incidents'}
                      </span>
                    </div>
                    <div class="text-xs text-slate-600 mt-2">
                      <span class="font-medium">Followed by:</span> {item.associatedSymptoms.join(', ')}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      );
    }

    ReactDOM.createRoot(document.getElementById('root')).render(<App />);
  </script>
</body>
</html>
""";
        return Response.ok(html).build();
    }
}

