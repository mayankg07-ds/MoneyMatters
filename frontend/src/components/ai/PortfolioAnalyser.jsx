import { useState } from 'react';
import { Sparkles, Loader2, Send, ChevronDown, ChevronUp } from 'lucide-react';
import { aiApi } from '../../services/api';

const SUGGESTED_QUESTIONS = [
  'Which stock should I consider selling first?',
  'Is my portfolio too concentrated in one sector?',
  'Am I beating Nifty50 returns?',
  'Which stock has the best growth potential?',
  'Should I add more stocks or switch to index funds?',
  'What is the biggest risk in my portfolio right now?',
  'Which stocks are trading below their 200-day average?',
  'Do I have enough dividend-paying stocks?',
];

const s = {
  card: {
    marginTop: 20, padding: 20, borderRadius: 16,
    background: 'var(--card-bg,#131a26)',
    border: '1px solid rgba(124,77,255,0.25)',
  },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' },
  titleWrap: { display: 'flex', alignItems: 'center', gap: 10 },
  title: { fontSize: 16, fontWeight: 700, color: 'var(--text)' },
  sub: { fontSize: 12, color: 'var(--text-muted)', marginTop: 2 },
  btn: {
    display: 'inline-flex', alignItems: 'center', gap: 8,
    background: 'linear-gradient(135deg,#7c4dff 0%,#00bcd4 100%)',
    color: '#fff', border: 'none', padding: '10px 18px', borderRadius: 10,
    fontWeight: 600, cursor: 'pointer', fontSize: 14,
  },
  body: {
    marginTop: 16, fontSize: 13.5, color: 'var(--text)',
    whiteSpace: 'pre-line', lineHeight: 1.65,
  },
  pillRow: { display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 14 },
  pill: {
    fontSize: 11.5, background: 'rgba(0,188,212,0.08)', color: '#6de4ff',
    border: '1px solid rgba(0,188,212,0.35)', padding: '6px 11px',
    borderRadius: 999, cursor: 'pointer',
  },
  inputRow: { display: 'flex', gap: 8, marginTop: 10 },
  input: {
    flex: 1, background: 'var(--bg-input,#0f1620)', border: '1px solid rgba(255,255,255,0.08)',
    color: 'var(--text)', borderRadius: 8, padding: '9px 12px', fontSize: 13,
  },
  sendBtn: {
    background: '#00bcd4', color: '#0a0f1a', border: 'none', padding: '9px 16px',
    borderRadius: 8, cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 6,
    fontSize: 13, fontWeight: 600,
  },
  followBox: {
    marginTop: 14, padding: 14,
    background: 'rgba(0,188,212,0.06)',
    border: '1px solid rgba(0,188,212,0.25)',
    borderRadius: 10,
  },
  label: { fontSize: 11, fontWeight: 700, color: '#00bcd4', marginBottom: 6, letterSpacing: 0.3 },
  quota: { fontSize: 11, color: 'var(--text-muted)', marginTop: 10 },
  err: { color: '#ff4c6a', fontSize: 13, marginTop: 10 },
  toggleRow: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' },
};

export default function PortfolioAnalyser() {
  const [open, setOpen] = useState(true);
  const [loading, setLoading] = useState(false);
  const [analysis, setAnalysis] = useState('');
  const [remaining, setRemaining] = useState(null);
  const [error, setError] = useState('');
  const [followUp, setFollowUp] = useState('');
  const [followAnswer, setFollowAnswer] = useState('');
  const [followLoading, setFollowLoading] = useState(false);

  const errMsg = (e) => {
    if (e?.response?.status === 429) {
      const d = e.response.data || {};
      return `You've hit the AI limit (${d.limit || 10}/hour). Try again in ~${Math.ceil((d.retryAfterSeconds || 60) / 60)} min.`;
    }
    return 'AI is unavailable right now. Please try again.';
  };

  const analyse = async () => {
    setLoading(true); setError(''); setAnalysis(''); setFollowAnswer('');
    try {
      const res = await aiApi.analysePortfolio();
      const data = res.data.data;
      setAnalysis(data.explanation);
      setRemaining(data.remainingRequests);
    } catch (e) {
      setError(errMsg(e));
    }
    setLoading(false);
  };

  const askFollowUp = async (question) => {
    const q = (question || '').trim();
    if (!q || !analysis) return;
    setFollowLoading(true); setError(''); setFollowAnswer('');
    try {
      const res = await aiApi.followup('PORTFOLIO', analysis, q);
      const data = res.data.data;
      setFollowAnswer(data.explanation);
      setRemaining(data.remainingRequests);
    } catch (e) {
      setError(errMsg(e));
    }
    setFollowLoading(false);
  };

  return (
    <div style={s.card}>
      <div style={s.toggleRow} onClick={() => setOpen(!open)}>
        <div style={s.titleWrap}>
          <Sparkles size={20} color="#7c4dff" />
          <div>
            <div style={s.title}>AI Portfolio Analysis</div>
            <div style={s.sub}>Powered by NVIDIA Nemotron — fundamentals, risks, and actionable insights</div>
          </div>
        </div>
        {open ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
      </div>

      {open && (
        <>
          <div style={{ ...s.header, marginTop: 14 }}>
            <div style={s.sub}>
              {analysis ? 'Analysis ready. Ask follow-ups below.' : 'Click to run an AI analysis on your holdings.'}
            </div>
            <button style={s.btn} onClick={analyse} disabled={loading}>
              {loading ? <Loader2 size={16} className="spin" /> : <Sparkles size={16} />}
              {loading ? 'Analysing portfolio...' : analysis ? 'Re-analyse' : 'Analyse my portfolio'}
            </button>
          </div>

          {error && <div style={s.err}>{error}</div>}

          {analysis && (
            <>
              <div style={s.body}>{analysis}</div>
              {remaining != null && <div style={s.quota}>{remaining} AI requests left this hour</div>}

              <div style={{ ...s.label, marginTop: 16 }}>ASK A FOLLOW-UP</div>
              <div style={s.pillRow}>
                {SUGGESTED_QUESTIONS.map((q) => (
                  <button key={q} style={s.pill}
                    onClick={() => { setFollowUp(q); askFollowUp(q); }}>
                    {q}
                  </button>
                ))}
              </div>

              <div style={s.inputRow}>
                <input style={s.input} type="text" value={followUp}
                  onChange={(e) => setFollowUp(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && askFollowUp(followUp)}
                  placeholder="Ask anything about your portfolio..." />
                <button style={s.sendBtn} onClick={() => askFollowUp(followUp)} disabled={followLoading}>
                  {followLoading ? <Loader2 size={14} className="spin" /> : <Send size={14} />} Ask
                </button>
              </div>

              {followAnswer && (
                <div style={s.followBox}>
                  <div style={s.label}>AI ANSWER</div>
                  <div style={s.body}>{followAnswer}</div>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}
