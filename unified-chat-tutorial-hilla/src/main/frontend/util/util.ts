export function hashCode(s: string): number {
    let hash = 0;
    for (let i = 0; i < s.length; i++) {
        hash = s.charCodeAt(i) + ((hash << 5) - hash);
    }
    return hash;
}

export function formatDate(date: Date): string {
    return date.toLocaleString();
}