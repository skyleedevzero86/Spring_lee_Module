import { Button } from '@/components/ui/button'
import { useStore } from '@/store/useStore'

function App() {
  const { count, increment, decrement } = useStore()

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-4xl font-bold text-foreground mb-4">
          Pay Front
        </h1>
        <p className="text-muted-foreground mb-6">
          React + TypeScript + Vite + Tailwind + shadcn/ui + Zustand
        </p>
        
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <Button onClick={decrement} variant="outline" size="lg">
              -
            </Button>
            <span className="text-2xl font-semibold">{count}</span>
            <Button onClick={increment} variant="default" size="lg">
              +
            </Button>
          </div>
          
          <div className="flex gap-2">
            <Button variant="default">Default</Button>
            <Button variant="secondary">Secondary</Button>
            <Button variant="outline">Outline</Button>
            <Button variant="ghost">Ghost</Button>
            <Button variant="destructive">Destructive</Button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App

